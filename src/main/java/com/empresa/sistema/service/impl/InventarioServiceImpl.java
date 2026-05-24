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
        return toDTO(inventarioRepository.findByProducto_IdProductoAndSucursal_IdSucursal(idProducto, idSucursal)
                .orElseThrow(() -> new RuntimeException("Inventario no encontrado")));
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
        inv.setStockMinimo(dto.getStockMinimo());
        inv.setUltimaActualizacion(LocalDateTime.now());
        return toDTO(inventarioRepository.save(inv));
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

    private InventarioResponseDTO toDTO(Inventario i) {
        String estado = i.getCantidad() <= 0 ? "SIN STOCK"
                : i.getCantidad() <= i.getStockMinimo() ? "STOCK BAJO" : "OK";
        return InventarioResponseDTO.builder()
                .idInventario(i.getIdInventario())
                .producto(i.getProducto().getNombre())
                .codigoProducto(i.getProducto().getCodigo())
                .sucursal(i.getSucursal().getNombre())
                .cantidad(i.getCantidad())
                .stockMinimo(i.getStockMinimo())
                .estadoStock(estado)
                .ultimaActualizacion(i.getUltimaActualizacion())
                .build();
    }
}
