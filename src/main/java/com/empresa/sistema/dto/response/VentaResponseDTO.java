package com.empresa.sistema.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VentaResponseDTO {
    private Integer idVenta;
    private String numeroVenta;
    private String cliente;
    private String identificacionCliente;
    private String usuario;
    private String sucursal;
    private LocalDateTime fechaVenta;
    private BigDecimal subtotal;
    private BigDecimal ivaValor;
    private BigDecimal total;
    private String metodoPago;
    private String estado;
    private String observacion;
    private List<DetalleVentaResponseDTO> detalles;
}
