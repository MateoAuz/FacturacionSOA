package com.empresa.sistema.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class InventarioRequestDTO {
    @NotNull private Integer idProducto;
    @NotNull private Integer idSucursal;
    @NotNull @Min(0) private Integer cantidad;
}
