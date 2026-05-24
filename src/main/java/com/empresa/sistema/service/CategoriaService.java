package com.empresa.sistema.service;

import com.empresa.sistema.dto.request.CategoriaRequestDTO;
import com.empresa.sistema.dto.response.CategoriaResponseDTO;
import java.util.List;

public interface CategoriaService {
    List<CategoriaResponseDTO> listarTodas();
    CategoriaResponseDTO buscarPorId(Integer id);
    CategoriaResponseDTO crear(CategoriaRequestDTO dto);
    CategoriaResponseDTO actualizar(Integer id, CategoriaRequestDTO dto);
    void eliminar(Integer id);
}
