package com.empresa.sistema.dto.response;
import lombok.*;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class InventarioResponseDTO {
    private Integer idInventario;
    private String producto;
    private String codigoProducto;
    private String sucursal;
    private Integer cantidad;
    private String estadoStock;
    private LocalDateTime ultimaActualizacion;
}
