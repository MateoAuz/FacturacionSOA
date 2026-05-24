package com.empresa.sistema.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class DetalleVentaRequestDTO {
    @NotNull private Integer idProducto;
    @NotNull @Min(1) private Integer cantidad;
}
