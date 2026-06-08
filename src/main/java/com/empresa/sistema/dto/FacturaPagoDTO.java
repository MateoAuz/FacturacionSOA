package com.empresa.sistema.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

/** Usado tanto en request (crear) como en response (leer). */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FacturaPagoDTO {
    @NotBlank
    private String metodo;   // EFECTIVO | TARJETA | TRANSFERENCIA

    @NotNull @DecimalMin("0.01")
    private BigDecimal monto;
}
