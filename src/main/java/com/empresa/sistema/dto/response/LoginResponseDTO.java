package com.empresa.sistema.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String tipo;
    private String username;
    private String rol;
    private Long expiracion;
    private Integer idSucursal;
    private String sucursal;
}