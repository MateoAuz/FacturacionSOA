package com.empresa.sistema.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class ProductoRequestDTO {
    @NotBlank @Size(max=20) private String codigo;
    @NotBlank @Size(max=80) private String nombre;
    private String descripcion;
    private Integer idCategoria;
    @NotNull @DecimalMin("0.01") private BigDecimal precioVenta;
    private String unidadMedida = "UNIDAD";
    private Boolean aplicaIva = true;
    private String tipoSri = "BIEN";
}
