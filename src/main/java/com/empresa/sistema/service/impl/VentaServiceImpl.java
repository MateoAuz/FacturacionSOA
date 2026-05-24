package com.empresa.sistema.service.impl;

import com.empresa.sistema.dto.request.VentaRequestDTO;
import com.empresa.sistema.dto.response.DetalleVentaResponseDTO;
import com.empresa.sistema.dto.response.VentaResponseDTO;
import com.empresa.sistema.entity.*;
import com.empresa.sistema.repository.*;
import com.empresa.sistema.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final SucursalRepository sucursalRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final ConfiguracionIvaRepository configuracionIvaRepository;

    @Override
    public List<VentaResponseDTO> listarTodas() {
        return ventaRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public VentaResponseDTO buscarPorId(Integer id) {
        return toDTO(ventaRepository.findById(id).orElseThrow(() -> new RuntimeException("Venta no encontrada: " + id)));
    }

    @Override
    public VentaResponseDTO crear(VentaRequestDTO dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        Sucursal sucursal = sucursalRepository.findById(dto.getIdSucursal())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        ConfiguracionIva iva = configuracionIvaRepository.findByActivoTrue()
                .orElseThrow(() -> new RuntimeException("Configuración IVA no encontrada"));

        String numeroVenta = "VTA-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Venta venta = Venta.builder()
                .numeroVenta(numeroVenta).cliente(cliente).usuario(usuario)
                .sucursal(sucursal).configuracionIva(iva)
                .metodoPago(Venta.MetodoPago.valueOf(dto.getMetodoPago()))
                .observacion(dto.getObservacion())
                .estado(Venta.EstadoVenta.PAGADA)
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        List<DetalleVenta> detalles = new ArrayList<>();

        for (var detalleDto : dto.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDto.getIdProducto())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            BigDecimal subtotalLinea = producto.getPrecioVenta()
                    .multiply(BigDecimal.valueOf(detalleDto.getCantidad()));
            subtotal = subtotal.add(subtotalLinea);
            detalles.add(DetalleVenta.builder()
                    .venta(venta).producto(producto)
                    .cantidad(detalleDto.getCantidad())
                    .precioUnitario(producto.getPrecioVenta())
                    .subtotalLinea(subtotalLinea).build());
        }

        BigDecimal ivaValor = subtotal.multiply(iva.getPorcentaje().divide(BigDecimal.valueOf(100)));
        venta.setSubtotal(subtotal);
        venta.setIvaValor(ivaValor);
        venta.setTotal(subtotal.add(ivaValor));

        Venta saved = ventaRepository.save(venta);
        detalles.forEach(d -> d.setVenta(saved));
        detalleVentaRepository.saveAll(detalles);

        return toDTO(saved);
    }

    @Override
    public void anular(Integer id) {
        Venta v = ventaRepository.findById(id).orElseThrow(() -> new RuntimeException("Venta no encontrada: " + id));
        v.setEstado(Venta.EstadoVenta.ANULADA);
        ventaRepository.save(v);
    }

    @Override
    public List<VentaResponseDTO> listarPorSucursal(Integer idSucursal) {
        return ventaRepository.findBySucursal_IdSucursal(idSucursal)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<VentaResponseDTO> listarPorFecha(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaVentaBetween(inicio, fin)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<VentaResponseDTO> listarPorCliente(Integer idCliente) {
        return ventaRepository.findByCliente_IdCliente(idCliente)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private VentaResponseDTO toDTO(Venta v) {
        List<DetalleVentaResponseDTO> detalles = detalleVentaRepository.findByVenta_IdVenta(v.getIdVenta())
                .stream().map(d -> DetalleVentaResponseDTO.builder()
                        .idDetalle(d.getIdDetalle())
                        .producto(d.getProducto().getNombre())
                        .codigoProducto(d.getProducto().getCodigo())
                        .cantidad(d.getCantidad())
                        .precioUnitario(d.getPrecioUnitario())
                        .subtotalLinea(d.getSubtotalLinea()).build())
                .collect(Collectors.toList());

        return VentaResponseDTO.builder()
                .idVenta(v.getIdVenta()).numeroVenta(v.getNumeroVenta())
                .cliente(v.getCliente().getNombres() + " " + (v.getCliente().getApellidos() != null ? v.getCliente().getApellidos() : ""))
                .identificacionCliente(v.getCliente().getIdentificacion())
                .usuario(v.getUsuario().getNombre() + " " + v.getUsuario().getApellido())
                .sucursal(v.getSucursal().getNombre()).fechaVenta(v.getFechaVenta())
                .subtotal(v.getSubtotal()).ivaValor(v.getIvaValor()).total(v.getTotal())
                .metodoPago(v.getMetodoPago().name()).estado(v.getEstado().name())
                .observacion(v.getObservacion()).detalles(detalles).build();
    }
}
