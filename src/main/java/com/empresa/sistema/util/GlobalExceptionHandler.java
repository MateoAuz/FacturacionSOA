package com.empresa.sistema.util;

import com.empresa.sistema.dto.response.ApiResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleRuntime(RuntimeException ex) {
        log.error("Error de negocio: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.error(ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponseDTO.error("Credenciales incorrectas"));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponseDTO.error("Acceso denegado: no tiene permisos para esta operación"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String campo = ((FieldError) error).getField();
            errores.put(campo, error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponseDTO.<Map<String, String>>builder()
                        .exitoso(false).mensaje("Error de validación").datos(errores).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleGeneral(Exception ex) {
        log.error("Error inesperado: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.error("Error interno del servidor"));
    }
}
