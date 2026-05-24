package com.empresa.sistema.service;

import com.empresa.sistema.dto.request.InventarioRequestDTO;
import com.empresa.sistema.dto.response.InventarioResponseDTO;
import java.util.List;

public interface InventarioService {
    List<InventarioResponseDTO> listarPorSucursal(Integer idSucursal);
    InventarioResponseDTO buscarPorProductoYSucursal(Integer idProducto, Integer idSucursal);
    InventarioResponseDTO actualizarStock(InventarioRequestDTO dto);
    void ajustarStock(Integer idProducto, Integer idSucursal, Integer cantidad);
}
