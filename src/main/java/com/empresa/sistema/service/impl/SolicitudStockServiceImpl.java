package com.empresa.sistema.service.impl;

import com.empresa.sistema.dto.request.SolicitudStockRequestDTO;
import com.empresa.sistema.dto.response.SolicitudStockResponseDTO;
import com.empresa.sistema.entity.*;
import com.empresa.sistema.repository.*;
import com.empresa.sistema.service.EmailService;
import com.empresa.sistema.service.InventarioService;
import com.empresa.sistema.service.SolicitudStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SolicitudStockServiceImpl implements SolicitudStockService {

    private final SolicitudStockRepository solicitudRepository;
    private final UsuarioRepository        usuarioRepository;
    private final ProductoRepository       productoRepository;
    private final SucursalRepository       sucursalRepository;
    private final InventarioService        inventarioService;
    private final EmailService             emailService;

    @Override
    public SolicitudStockResponseDTO crear(SolicitudStockRequestDTO dto, String username) {
        Usuario solicitante = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Producto producto = productoRepository.findById(dto.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Sucursal sucursalProveedora = sucursalRepository.findById(dto.getIdSucursalProveedora())
                .orElseThrow(() -> new RuntimeException("Sucursal proveedora no encontrada"));

        Sucursal sucursalSolicitante = solicitante.getSucursal();
        if (sucursalSolicitante == null) {
            // ADMIN no tiene sucursal fija — usar la que envió el frontend
            if (dto.getIdSucursalSolicitante() == null) {
                throw new RuntimeException("Selecciona la sucursal desde la que solicitas el stock.");
            }
            sucursalSolicitante = sucursalRepository.findById(dto.getIdSucursalSolicitante())
                    .orElseThrow(() -> new RuntimeException("Sucursal solicitante no encontrada"));
        }

        if (sucursalSolicitante.getIdSucursal().equals(sucursalProveedora.getIdSucursal())) {
            throw new RuntimeException("No puedes solicitar stock a tu propia sucursal");
        }

        // Verificar que no exista ya una solicitud PENDIENTE para el mismo producto/sucursal
        boolean yaExiste = solicitudRepository
                .existsByProducto_IdProductoAndSucursalSolicitante_IdSucursalAndSucursalProveedora_IdSucursalAndEstado(
                        dto.getIdProducto(), sucursalSolicitante.getIdSucursal(),
                        sucursalProveedora.getIdSucursal(), SolicitudStock.Estado.PENDIENTE);
        if (yaExiste) {
            throw new RuntimeException("Ya tienes una solicitud pendiente para este producto desde esa sucursal. Espera a que sea procesada.");
        }

        // Verificar stock disponible real vs cantidad ya comprometida en solicitudes pendientes
        int stockActual = inventarioService.obtenerStockActual(
                dto.getIdProducto(), sucursalProveedora.getIdSucursal());
        int comprometido = solicitudRepository.sumCantidadPendienteByProductoAndProveedora(
                dto.getIdProducto(), sucursalProveedora.getIdSucursal());
        int disponible = stockActual - comprometido;
        if (dto.getCantidad() > disponible) {
            throw new RuntimeException(
                    "Stock insuficiente: la sucursal proveedora tiene " + stockActual +
                    " unidad(es), pero " + comprometido + " ya están comprometidas en otras solicitudes pendientes. " +
                    "Disponible real: " + disponible);
        }

        SolicitudStock solicitud = SolicitudStock.builder()
                .producto(producto)
                .sucursalSolicitante(sucursalSolicitante)
                .sucursalProveedora(sucursalProveedora)
                .cantidad(dto.getCantidad())
                .estado(SolicitudStock.Estado.PENDIENTE)
                .usuarioSolicitante(solicitante)
                .observacion(dto.getObservacion())
                .fechaSolicitud(LocalDateTime.now())
                .build();

        solicitud = solicitudRepository.save(solicitud);

        // Notificar a bodegueros de la sucursal proveedora
        try {
            List<Usuario> bodegueros = usuarioRepository
                    .findByRol_NombreAndSucursal_IdSucursalAndActivoTrue("BODEGUERO", sucursalProveedora.getIdSucursal());
            emailService.enviarSolicitudStock(solicitud, bodegueros);
        } catch (Exception e) {
            log.warn("[SolicitudStock] Error enviando correo de notificación: {}", e.getMessage());
        }

        return toDTO(solicitud);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudStockResponseDTO> listarPendientes(String username) {
        Usuario bodeguero = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return solicitudRepository
                .findBySucursalProveedora_IdSucursalAndEstado(
                        bodeguero.getSucursal().getIdSucursal(), SolicitudStock.Estado.PENDIENTE)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudStockResponseDTO> listarPorSucursalSolicitante(Integer idSucursal) {
        return solicitudRepository
                .findBySucursalSolicitante_IdSucursalOrderByFechaSolicitudDesc(idSucursal)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public SolicitudStockResponseDTO aceptar(Integer idSolicitud, String username) {
        SolicitudStock solicitud = findAndValidate(idSolicitud, username);

        // Verificar stock suficiente en proveedora antes de transferir
        int stockProveedora = inventarioService.obtenerStockActual(
                solicitud.getProducto().getIdProducto(),
                solicitud.getSucursalProveedora().getIdSucursal());
        if (stockProveedora < solicitud.getCantidad()) {
            throw new RuntimeException(
                    "Stock insuficiente en la sucursal proveedora: tiene " + stockProveedora +
                    " unidad(es), se solicitan " + solicitud.getCantidad());
        }

        // Transferir stock: descontar de proveedora, añadir a solicitante
        inventarioService.ajustarStock(
                solicitud.getProducto().getIdProducto(),
                solicitud.getSucursalProveedora().getIdSucursal(),
                -solicitud.getCantidad());
        inventarioService.ajustarStock(
                solicitud.getProducto().getIdProducto(),
                solicitud.getSucursalSolicitante().getIdSucursal(),
                solicitud.getCantidad());

        Usuario responde = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        solicitud.setEstado(SolicitudStock.Estado.ACEPTADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setUsuarioRespuesta(responde);

        return toDTO(solicitudRepository.save(solicitud));
    }

    @Override
    public SolicitudStockResponseDTO rechazar(Integer idSolicitud, String username) {
        SolicitudStock solicitud = findAndValidate(idSolicitud, username);

        Usuario responde = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        solicitud.setEstado(SolicitudStock.Estado.RECHAZADA);
        solicitud.setFechaRespuesta(LocalDateTime.now());
        solicitud.setUsuarioRespuesta(responde);

        return toDTO(solicitudRepository.save(solicitud));
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPendientes(String username) {
        Usuario bodeguero = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (bodeguero.getSucursal() == null) return 0;
        return solicitudRepository.countPendientesByProveedora(bodeguero.getSucursal().getIdSucursal());
    }

    // ─────────────────────────────────────────────────────────────────────────

    private SolicitudStock findAndValidate(Integer idSolicitud, String username) {
        SolicitudStock solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        if (solicitud.getEstado() != SolicitudStock.Estado.PENDIENTE) {
            throw new RuntimeException("La solicitud ya fue procesada");
        }
        Usuario bodeguero = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (!solicitud.getSucursalProveedora().getIdSucursal()
                .equals(bodeguero.getSucursal().getIdSucursal())) {
            throw new RuntimeException("No tienes permiso para procesar esta solicitud");
        }
        return solicitud;
    }

    private SolicitudStockResponseDTO toDTO(SolicitudStock s) {
        String respuesta = s.getUsuarioRespuesta() != null
                ? s.getUsuarioRespuesta().getNombre() + " " + s.getUsuarioRespuesta().getApellido()
                : null;
        return SolicitudStockResponseDTO.builder()
                .idSolicitud(s.getIdSolicitud())
                .idProducto(s.getProducto().getIdProducto())
                .producto(s.getProducto().getNombre())
                .codigoProducto(s.getProducto().getCodigo())
                .idSucursalSolicitante(s.getSucursalSolicitante().getIdSucursal())
                .sucursalSolicitante(s.getSucursalSolicitante().getNombre())
                .idSucursalProveedora(s.getSucursalProveedora().getIdSucursal())
                .sucursalProveedora(s.getSucursalProveedora().getNombre())
                .cantidad(s.getCantidad())
                .estado(s.getEstado().name())
                .usuarioSolicitante(s.getUsuarioSolicitante().getNombre() + " " + s.getUsuarioSolicitante().getApellido())
                .observacion(s.getObservacion())
                .fechaSolicitud(s.getFechaSolicitud())
                .fechaRespuesta(s.getFechaRespuesta())
                .usuarioRespuesta(respuesta)
                .build();
    }
}
