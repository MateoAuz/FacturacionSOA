package com.empresa.sistema.dto.response;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CategoriaResponseDTO {
    private Integer idCategoria;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
