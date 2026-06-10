package com.empresa.sistema.dto.response;

import com.empresa.sistema.dto.FacturaPagoDTO;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FacturaResponseDTO {
    private Integer idFactura;
    private String  numeroSecuencial;

    // Datos del cliente
    private String  cliente;
    private String  identificacionCliente;
    private String  correoCliente;

    // Datos de la emisión
    private String  usuario;
    private String  sucursal;
    private Integer idSucursal;
    private LocalDateTime fechaFactura;

    // Económicos
    private BigDecimal subtotal;
    private BigDecimal ivaValor;
    private BigDecimal total;
    private String     metodoPago;
    private String     observacion;

    // Estado
    private String  estado;
    private String  estadoSri;

    // SRI
    private LocalDateTime fechaEmision;
    private String        pdfPath;
    private String        claveAcceso;

    // Detalles de productos
    private List<DetalleVentaResponseDTO> detalles;

    // Formas de pago (multi-pago)
    private List<FacturaPagoDTO> pagos;
}
