package com.empresa.sistema.controller;

import com.empresa.sistema.dto.response.ApiResponseDTO;
import com.empresa.sistema.dto.response.FacturaResponseDTO;
import com.empresa.sistema.service.FacturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.empresa.sistema.dto.response.PageResponseDTO;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
@Tag(name = "Facturas", description = "Gestión de facturas")
@SecurityRequirement(name = "bearerAuth")
public class FacturaController {

    private final FacturaService facturaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Listar todas las facturas")
    public ResponseEntity<ApiResponseDTO<List<FacturaResponseDTO>>> listar() {
        return ResponseEntity.ok(ApiResponseDTO.ok(facturaService.listarTodas()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Obtener factura por ID")
    public ResponseEntity<ApiResponseDTO<FacturaResponseDTO>> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponseDTO.ok(facturaService.buscarPorId(id)));
    }

    @PostMapping("/generar/{idVenta}")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Generar factura para una venta")
    public ResponseEntity<ApiResponseDTO<FacturaResponseDTO>> generar(@PathVariable Integer idVenta) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Factura generada", facturaService.generarFactura(idVenta)));
    }

    @GetMapping("/paginado")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @Operation(summary = "Listar facturas paginado con búsqueda y filtros")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<FacturaResponseDTO>>> buscarPaginado(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponseDTO.ok(facturaService.buscarPaginado(search, estado, page, size)));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Descargar PDF de factura")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Integer id) {
        byte[] pdf = facturaService.generarPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "factura-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Anular factura")
    public ResponseEntity<ApiResponseDTO<Void>> anular(@PathVariable Integer id) {
        facturaService.anular(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Factura anulada", null));
    }
}
