package com.empresa.sistema.service;

import com.empresa.sistema.dto.request.VentaRequestDTO;
import com.empresa.sistema.dto.response.VentaResponseDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface VentaService {
    List<VentaResponseDTO> listarTodas();
    VentaResponseDTO buscarPorId(Integer id);
    VentaResponseDTO crear(VentaRequestDTO dto);
    void anular(Integer id);
    List<VentaResponseDTO> listarPorSucursal(Integer idSucursal);
    List<VentaResponseDTO> listarPorFecha(LocalDateTime inicio, LocalDateTime fin);
    List<VentaResponseDTO> listarPorCliente(Integer idCliente);
}
