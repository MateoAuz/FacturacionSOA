package com.empresa.sistema.controller;

import com.empresa.sistema.dto.request.FacturaRequestDTO;
import com.empresa.sistema.dto.response.ApiResponseDTO;
import com.empresa.sistema.dto.response.FacturaResponseDTO;
import com.empresa.sistema.dto.response.PageResponseDTO;
import com.empresa.sistema.service.FacturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
@Tag(name = "Facturas", description = "Gestión completa de facturas")
@SecurityRequirement(name = "bearerAuth")
public class FacturaController {

    private final FacturaService facturaService;

    // ── Crear factura (unificado venta + factura) ─────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Crear nueva factura (genera número, valida stock y descuenta)")
    public ResponseEntity<ApiResponseDTO<FacturaResponseDTO>> crear(
            @Valid @RequestBody FacturaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Factura creada", facturaService.crear(dto)));
    }

    // ── Consultas ─────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Listar todas las facturas")
    public ResponseEntity<ApiResponseDTO<List<FacturaResponseDTO>>> listar() {
        return ResponseEntity.ok(ApiResponseDTO.ok(facturaService.listarTodas()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Obtener factura por ID (incluye detalles)")
    public ResponseEntity<ApiResponseDTO<FacturaResponseDTO>> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponseDTO.ok(facturaService.buscarPorId(id)));
    }

    @GetMapping("/paginado")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Listar facturas paginado con filtros")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<FacturaResponseDTO>>> buscarPaginado(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponseDTO.ok(
                facturaService.buscarPaginado(search, estado, idSucursal, page, size)));
    }

    // ── Ciclo de vida ─────────────────────────────────────────────
    @PostMapping("/{id}/emitir")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Emitir factura (GUARDADA → EMITIDA)")
    public ResponseEntity<ApiResponseDTO<Void>> emitir(@PathVariable Integer id) {
        facturaService.emitirFactura(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Factura emitida", null));
    }

    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Anular factura y devolver stock")
    public ResponseEntity<ApiResponseDTO<Void>> anular(@PathVariable Integer id) {
        facturaService.anularFactura(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Factura anulada", null));
    }

    // ── Utilidades ───────────────────────────────────────────────
    @GetMapping("/proximo-numero")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Vista previa del próximo número de factura para una sucursal")
    public ResponseEntity<ApiResponseDTO<String>> proximoNumero(@RequestParam Integer idSucursal) {
        return ResponseEntity.ok(ApiResponseDTO.ok(facturaService.proximoNumero(idSucursal)));
    }

    // ── PDF ───────────────────────────────────────────────────────
    // Sin @PreAuthorize: el acceso público está permitido en SecurityConfig
    // para que el enlace del correo al cliente funcione sin login.
    @GetMapping("/{id}/pdf")
    @Operation(summary = "Descargar PDF de factura (público — enlace de correo)")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Integer id) {
        byte[] pdf = facturaService.generarPdf(id);
        FacturaResponseDTO factura = facturaService.buscarPorId(id);
        String filename = "Factura_" + factura.getNumeroSecuencial().replace("/", "-") + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", filename);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
