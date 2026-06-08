package com.empresa.sistema.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data
public class ClienteRequestDTO {
    @NotBlank private String tipoIdentificacion;
    @NotBlank @Size(min=5, max=20) private String identificacion;
    @NotBlank private String nombres;
    private String apellidos;
    private String razonSocial;
    private String direccion;
    private String telefono;
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "Formato de correo inválido") private String correo;
}
