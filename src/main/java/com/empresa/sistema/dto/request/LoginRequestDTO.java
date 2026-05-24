package com.empresa.sistema.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class LoginRequestDTO {
    @NotBlank(message = "El username es requerido")
    private String username;
    @NotBlank(message = "La contraseña es requerida")
    private String password;
}
