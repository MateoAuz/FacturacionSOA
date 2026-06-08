package com.empresa.sistema.service;

import com.empresa.sistema.dto.request.FacturaRequestDTO;
import com.empresa.sistema.dto.response.FacturaResponseDTO;
import com.empresa.sistema.dto.response.PageResponseDTO;
import java.util.List;

public interface FacturaService {
    // ── Ciclo de vida de la factura ──────────────────────────────
    FacturaResponseDTO crear(FacturaRequestDTO dto);
    FacturaResponseDTO buscarPorId(Integer id);
    void emitirFactura(Integer id);
    void anularFactura(Integer id);

    // ── Consultas ────────────────────────────────────────────────
    List<FacturaResponseDTO>              listarTodas();
    List<FacturaResponseDTO>              listarPorSucursal(Integer idSucursal);
    PageResponseDTO<FacturaResponseDTO>   buscarPaginado(String search, String estado,
                                                         Integer idSucursal, int page, int size);

    // ── Utilidades ───────────────────────────────────────────────
    /** Devuelve el próximo número secuencial (solo vista previa; el real se asigna al crear). */
    String proximoNumero(Integer idSucursal);

    // ── PDF ──────────────────────────────────────────────────────
    byte[] generarPdf(Integer idFactura);
}
