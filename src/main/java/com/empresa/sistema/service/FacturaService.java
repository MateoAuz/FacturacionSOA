package com.empresa.sistema.service;

import com.empresa.sistema.dto.response.FacturaResponseDTO;
import java.util.List;
import com.empresa.sistema.dto.response.PageResponseDTO;

public interface FacturaService {
    FacturaResponseDTO generarFactura(Integer idVenta);
    FacturaResponseDTO buscarPorId(Integer id);
    FacturaResponseDTO buscarPorVenta(Integer idVenta);
    byte[] generarPdf(Integer idFactura);
    List<FacturaResponseDTO> listarTodas();
    void anular(Integer id);
    PageResponseDTO<FacturaResponseDTO> buscarPaginado(String search, String estado, int page, int size);
}
