package com.empresa.sistema.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class UsuarioRequestDTO {
    @NotBlank private String nombre;
    @NotBlank private String apellido;
    @NotBlank @Size(min=3, max=30) private String username;
    @NotBlank @Size(min=6) private String password;
    @Email private String correo;
    @NotNull private Integer idRol;
    @NotNull private Integer idSucursal;
}
