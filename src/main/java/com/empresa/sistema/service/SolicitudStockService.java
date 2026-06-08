package com.empresa.sistema.service;

import com.empresa.sistema.dto.request.SolicitudStockRequestDTO;
import com.empresa.sistema.dto.response.SolicitudStockResponseDTO;

import java.util.List;

public interface SolicitudStockService {

    /** CAJERO crea una solicitud de stock desde otra sucursal */
    SolicitudStockResponseDTO crear(SolicitudStockRequestDTO dto, String username);

    /** BODEGUERO ve las solicitudes pendientes dirigidas a su sucursal */
    List<SolicitudStockResponseDTO> listarPendientes(String username);

    /** Todas las solicitudes (para admin o vista historial) */
    List<SolicitudStockResponseDTO> listarPorSucursalSolicitante(Integer idSucursal);

    /** BODEGUERO acepta: transfiere stock y actualiza estado */
    SolicitudStockResponseDTO aceptar(Integer idSolicitud, String username);

    /** BODEGUERO rechaza: solo actualiza estado */
    SolicitudStockResponseDTO rechazar(Integer idSolicitud, String username);

    /** Cuenta de solicitudes pendientes para el BODEGUERO (badge) */
    long contarPendientes(String username);
}
