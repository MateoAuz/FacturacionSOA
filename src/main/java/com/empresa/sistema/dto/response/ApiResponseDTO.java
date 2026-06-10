package com.empresa.sistema.dto.response;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ApiResponseDTO<T> {
    private boolean exitoso;
    private String mensaje;
    private T datos;
    private LocalDateTime timestamp = LocalDateTime.now();
    public static <T> ApiResponseDTO<T> ok(T datos) {
        return ApiResponseDTO.<T>builder().exitoso(true).datos(datos).timestamp(LocalDateTime.now()).build();
    }
    public static <T> ApiResponseDTO<T> ok(String mensaje, T datos) {
        return ApiResponseDTO.<T>builder().exitoso(true).mensaje(mensaje).datos(datos).timestamp(LocalDateTime.now()).build();
    }
    public static <T> ApiResponseDTO<T> error(String mensaje) {
        return ApiResponseDTO.<T>builder().exitoso(false).mensaje(mensaje).timestamp(LocalDateTime.now()).build();
    }
}
