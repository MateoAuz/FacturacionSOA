package com.empresa.sistema.service.impl;

import com.empresa.sistema.dto.request.InventarioRequestDTO;
import com.empresa.sistema.dto.response.InventarioResponseDTO;
import com.empresa.sistema.entity.Inventario;
import com.empresa.sistema.entity.Producto;
import com.empresa.sistema.entity.Sucursal;
import com.empresa.sistema.repository.InventarioRepository;
import com.empresa.sistema.repository.ProductoRepository;
import com.empresa.sistema.repository.SucursalRepository;
import com.empresa.sistema.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.empresa.sistema.dto.response.PageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
@Transactional
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final ProductoRepository productoRepository;
    private final SucursalRepository sucursalRepository;

    @Override
    public List<InventarioResponseDTO> listarPorSucursal(Integer idSucursal) {
        return inventarioRepository.findBySucursal_IdSucursal(idSucursal)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public InventarioResponseDTO buscarPorProductoYSucursal(Integer idProducto, Integer idSucursal) {
        return inventarioRepository
                .findByProducto_IdProductoAndSucursal_IdSucursal(idProducto, idSucursal)
                .map(this::toDTO)
                .orElseGet(() -> InventarioResponseDTO.builder()
                        .cantidad(0)
                        .estadoStock("SIN STOCK")
                        .build());
    }

    @Override
    public InventarioResponseDTO buscarPorId(Integer id) {
        return toDTO(inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado: " + id)));
    }

    @Override
    public InventarioResponseDTO actualizarStock(InventarioRequestDTO dto) {
        Producto producto = productoRepository.findById(dto.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        Sucursal sucursal = sucursalRepository.findById(dto.getIdSucursal())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        Inventario inv = inventarioRepository
                .findByProducto_IdProductoAndSucursal_IdSucursal(dto.getIdProducto(), dto.getIdSucursal())
                .orElse(Inventario.builder().producto(producto).sucursal(sucursal).build());
        inv.setCantidad(dto.getCantidad());
        inv.setUltimaActualizacion(LocalDateTime.now());
        return toDTO(inventarioRepository.save(inv));
    }
    @Override
    public PageResponseDTO<InventarioResponseDTO> buscarPaginado(String search, Integer idSucursal, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("producto.nombre").ascending());
        Page<Inventario> resultado = inventarioRepository.buscarPaginado(
                (search != null && !search.isBlank()) ? search : null,
                idSucursal,
                pageable);
        return PageResponseDTO.<InventarioResponseDTO>builder()
                .contenido(resultado.getContent().stream().map(this::toDTO).collect(Collectors.toList()))
                .paginaActual(resultado.getNumber())
                .totalPaginas(resultado.getTotalPages())
                .totalElementos(resultado.getTotalElements())
                .tamanioPagina(resultado.getSize())
                .primera(resultado.isFirst())
                .ultima(resultado.isLast())
                .build();
    }

    @Override
    public void ajustarStock(Integer idProducto, Integer idSucursal, Integer cantidad) {
        Inventario inv = inventarioRepository
                .findByProducto_IdProductoAndSucursal_IdSucursal(idProducto, idSucursal)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));
        inv.setCantidad(inv.getCantidad() + cantidad);
        inv.setUltimaActualizacion(LocalDateTime.now());
        inventarioRepository.save(inv);
    }

    @Override
    public List<InventarioResponseDTO> listarPorProducto(Integer idProducto) {
        return inventarioRepository.findByProducto_IdProducto(idProducto)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    private InventarioResponseDTO toDTO(Inventario i) {
        String estado = i.getCantidad() <= 0 ? "SIN STOCK" : "OK";
        return InventarioResponseDTO.builder()
                .idInventario(i.getIdInventario())
                .producto(i.getProducto().getNombre())
                .codigoProducto(i.getProducto().getCodigo())
                .sucursal(i.getSucursal().getNombre())
                .cantidad(i.getCantidad())
                .estadoStock(estado)
                .ultimaActualizacion(i.getUltimaActualizacion())
                .build();
    }
}
