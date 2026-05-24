package com.empresa.sistema.service;

import com.empresa.sistema.dto.request.ClienteRequestDTO;
import com.empresa.sistema.dto.response.ClienteResponseDTO;
import java.util.List;

public interface ClienteService {
    List<ClienteResponseDTO> listarTodos();
    ClienteResponseDTO buscarPorId(Integer id);
    ClienteResponseDTO buscarPorIdentificacion(String identificacion);
    ClienteResponseDTO crear(ClienteRequestDTO dto);
    ClienteResponseDTO actualizar(Integer id, ClienteRequestDTO dto);
    void eliminar(Integer id);
    List<ClienteResponseDTO> buscarPorNombre(String nombre);
}
