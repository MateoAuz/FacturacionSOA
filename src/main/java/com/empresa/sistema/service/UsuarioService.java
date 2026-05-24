package com.empresa.sistema.service;

import com.empresa.sistema.dto.request.UsuarioRequestDTO;
import com.empresa.sistema.dto.response.UsuarioResponseDTO;
import java.util.List;

public interface UsuarioService {
    List<UsuarioResponseDTO> listarTodos();
    UsuarioResponseDTO buscarPorId(Integer id);
    UsuarioResponseDTO crear(UsuarioRequestDTO dto);
    UsuarioResponseDTO actualizar(Integer id, UsuarioRequestDTO dto);
    void eliminar(Integer id);
    void cambiarEstado(Integer id, Boolean activo);
}
