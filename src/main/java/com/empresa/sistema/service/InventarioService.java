package com.empresa.sistema.service;

import com.empresa.sistema.dto.request.InventarioRequestDTO;
import com.empresa.sistema.dto.response.InventarioResponseDTO;
import java.util.List;
import com.empresa.sistema.dto.response.PageResponseDTO;
import com.empresa.sistema.dto.response.PageResponseDTO;

public interface InventarioService {
    List<InventarioResponseDTO> listarPorSucursal(Integer idSucursal);
    InventarioResponseDTO buscarPorProductoYSucursal(Integer idProducto, Integer idSucursal);
    InventarioResponseDTO actualizarStock(InventarioRequestDTO dto);
    void ajustarStock(Integer idProducto, Integer idSucursal, Integer cantidad);
    PageResponseDTO<InventarioResponseDTO> buscarPaginado(String search, Integer idSucursal, int page, int size);
    InventarioResponseDTO buscarPorId(Integer id);


}
