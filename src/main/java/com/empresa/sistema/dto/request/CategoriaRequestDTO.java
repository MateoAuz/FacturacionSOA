package com.empresa.sistema.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class CategoriaRequestDTO {
    @NotBlank @Size(max=40) private String nombre;
    private String descripcion;
}
