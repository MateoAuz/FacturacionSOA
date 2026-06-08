package com.empresa.sistema.dto.response;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductoResponseDTO {
    private Integer idProducto;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String categoria;
    private BigDecimal precioVenta;
    private String unidadMedida;
    private Boolean aplicaIva;
    private Boolean activo;
    private String tipoSri;
    private LocalDateTime fechaRegistro;
    /** Solo se rellena cuando la consulta filtra por sucursal */
    private Integer stockDisponible;
    private String categoriaNombre;
}
