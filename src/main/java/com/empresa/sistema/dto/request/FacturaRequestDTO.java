package com.empresa.sistema.dto.request;

import com.empresa.sistema.dto.FacturaPagoDTO;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

/** Request unificado para crear una factura (antes separado en VentaRequestDTO) */
@Data
public class FacturaRequestDTO {
    @NotNull private Integer idCliente;
    @NotNull private Integer idSucursal;
    private String metodoPago = "EFECTIVO";
    private String observacion;
    @NotEmpty private List<DetalleVentaRequestDTO> detalles;
    /** Formas de pago con monto. Si se envía, tiene prioridad sobre metodoPago. */
    private List<FacturaPagoDTO> pagos;
}
