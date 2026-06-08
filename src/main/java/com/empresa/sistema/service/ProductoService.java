package com.empresa.sistema.service;

import com.empresa.sistema.dto.request.ProductoRequestDTO;
import com.empresa.sistema.dto.response.PageResponseDTO;
import com.empresa.sistema.dto.response.ProductoResponseDTO;
import java.util.List;

public interface ProductoService {
    List<ProductoResponseDTO> listarTodos();
    ProductoResponseDTO buscarPorId(Integer id);
    ProductoResponseDTO buscarPorCodigo(String codigo);
    ProductoResponseDTO crear(ProductoRequestDTO dto);
    ProductoResponseDTO actualizar(Integer id, ProductoRequestDTO dto);
    void eliminar(Integer id);
    List<ProductoResponseDTO> buscarPorCategoria(Integer idCategoria);
    List<ProductoResponseDTO> buscarPorNombre(String nombre);
    PageResponseDTO<ProductoResponseDTO> buscarPaginado(String search, Integer idCategoria, int page, int size);
    PageResponseDTO<ProductoResponseDTO> buscarPaginado(String search, Integer idCategoria, Integer idSucursal, int page, int size);
}