package com.empresa.sistema.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SolicitudStockResponseDTO {
    private Integer idSolicitud;
    private Integer idProducto;
    private String  producto;
    private String  codigoProducto;
    private Integer idSucursalSolicitante;
    private String  sucursalSolicitante;
    private Integer idSucursalProveedora;
    private String  sucursalProveedora;
    private Integer cantidad;
    private String  estado;
    private String  usuarioSolicitante;
    private String  observacion;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;
    private String  usuarioRespuesta;
}
