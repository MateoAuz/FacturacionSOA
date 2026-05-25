package com.empresa.sistema.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FacturaResponseDTO {
    private Integer idFactura;
    private Integer idVenta;
    private String numeroSecuencial;
    private LocalDateTime fechaEmision;
    private String pdfPath;
    private String estado;
    private String estadoSri;
    private String claveAcceso;
    private String cliente;
    private String identificacionCliente;
    private BigDecimal total;
    private String sucursal;
}