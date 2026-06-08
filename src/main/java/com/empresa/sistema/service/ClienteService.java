package com.empresa.sistema.service;

import com.empresa.sistema.dto.request.ClienteRequestDTO;
import com.empresa.sistema.dto.response.ClienteResponseDTO;
import java.util.List;
import com.empresa.sistema.dto.response.PageResponseDTO;

public interface ClienteService {
    List<ClienteResponseDTO> listarTodos();
    ClienteResponseDTO buscarPorId(Integer id);
    ClienteResponseDTO buscarPorIdentificacion(String identificacion);
    ClienteResponseDTO crear(ClienteRequestDTO dto);
    ClienteResponseDTO actualizar(Integer id, ClienteRequestDTO dto);
    void eliminar(Integer id);
    void cambiarEstado(Integer id, Boolean activo);
    List<ClienteResponseDTO> buscarPorNombre(String nombre);
    PageResponseDTO<ClienteResponseDTO> buscarPaginado(String search, String tipo, int page, int size);
}
