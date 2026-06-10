package com.empresa.sistema.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class SolicitudStockRequestDTO {

    @NotNull(message = "El producto es requerido")
    private Integer idProducto;

    @NotNull(message = "La sucursal proveedora es requerida")
    private Integer idSucursalProveedora;

    // Opcional: enviado por el frontend cuando el usuario es ADMIN (sin sucursal fija)
    private Integer idSucursalSolicitante;

    @NotNull(message = "La cantidad es requerida")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    private String observacion;
}
