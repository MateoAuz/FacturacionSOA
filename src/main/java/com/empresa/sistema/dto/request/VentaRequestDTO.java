package com.empresa.sistema.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;
@Data
public class VentaRequestDTO {
    @NotNull private Integer idCliente;
    @NotNull private Integer idSucursal;
    private String metodoPago = "EFECTIVO";
    private String observacion;
    @NotEmpty private List<DetalleVentaRequestDTO> detalles;
}
