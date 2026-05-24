package com.empresa.sistema.dto.response;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ClienteResponseDTO {
    private Integer idCliente;
    private String tipoIdentificacion;
    private String identificacion;
    private String nombres;
    private String apellidos;
    private String razonSocial;
    private String direccion;
    private String telefono;
    private String correo;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
}
