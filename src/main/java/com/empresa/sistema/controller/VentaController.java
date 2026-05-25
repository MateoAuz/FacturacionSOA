package com.empresa.sistema.controller;

import com.empresa.sistema.dto.request.VentaRequestDTO;
import com.empresa.sistema.dto.response.ApiResponseDTO;
import com.empresa.sistema.dto.response.VentaResponseDTO;
import com.empresa.sistema.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import com.empresa.sistema.dto.response.PageResponseDTO;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Gestión de ventas")
@SecurityRequirement(name = "bearerAuth")
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Listar todas las ventas")
    public ResponseEntity<ApiResponseDTO<List<VentaResponseDTO>>> listar() {
        return ResponseEntity.ok(ApiResponseDTO.ok(ventaService.listarTodas()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Obtener venta por ID")
    public ResponseEntity<ApiResponseDTO<VentaResponseDTO>> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponseDTO.ok(ventaService.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Registrar nueva venta")
    public ResponseEntity<ApiResponseDTO<VentaResponseDTO>> crear(@Valid @RequestBody VentaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Venta registrada", ventaService.crear(dto)));
    }

    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Anular venta")
    public ResponseEntity<ApiResponseDTO<Void>> anular(@PathVariable Integer id) {
        ventaService.anular(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Venta anulada", null));
    }

    @GetMapping("/sucursal/{idSucursal}")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Listar ventas por sucursal")
    public ResponseEntity<ApiResponseDTO<List<VentaResponseDTO>>> porSucursal(@PathVariable Integer idSucursal) {
        return ResponseEntity.ok(ApiResponseDTO.ok(ventaService.listarPorSucursal(idSucursal)));
    }
    @GetMapping("/paginado")
    @PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
    @Operation(summary = "Listar ventas paginado con búsqueda y filtros")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<VentaResponseDTO>>> buscarPaginado(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponseDTO.ok(ventaService.buscarPaginado(search, idSucursal, estado, page, size)));
    }

    @GetMapping("/cliente/{idCliente}")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Listar ventas por cliente")
    public ResponseEntity<ApiResponseDTO<List<VentaResponseDTO>>> porCliente(@PathVariable Integer idCliente) {
        return ResponseEntity.ok(ApiResponseDTO.ok(ventaService.listarPorCliente(idCliente)));
    }

    @GetMapping("/fecha")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Listar ventas por rango de fechas")
    public ResponseEntity<ApiResponseDTO<List<VentaResponseDTO>>> porFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(ApiResponseDTO.ok(ventaService.listarPorFecha(inicio, fin)));
    }
}
