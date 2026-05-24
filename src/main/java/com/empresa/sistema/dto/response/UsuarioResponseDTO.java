package com.empresa.sistema.dto.response;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UsuarioResponseDTO {
    private Integer idUsuario;
    private String nombre;
    private String apellido;
    private String username;
    private String correo;
    private String rol;
    private String sucursal;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
}
