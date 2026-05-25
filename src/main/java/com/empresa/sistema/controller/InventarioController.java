package com.empresa.sistema.controller;

import com.empresa.sistema.dto.request.InventarioRequestDTO;
import com.empresa.sistema.dto.response.ApiResponseDTO;
import com.empresa.sistema.dto.response.InventarioResponseDTO;
import com.empresa.sistema.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.empresa.sistema.dto.response.PageResponseDTO;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
@Tag(name = "Inventario", description = "Gestión de inventario por sucursal")
@SecurityRequirement(name = "bearerAuth")
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping("/sucursal/{idSucursal}")
    @PreAuthorize("hasAnyRole('ADMIN','BODEGUERO')")
    @Operation(summary = "Listar inventario por sucursal")
    public ResponseEntity<ApiResponseDTO<List<InventarioResponseDTO>>> listarPorSucursal(
            @PathVariable Integer idSucursal) {
        return ResponseEntity.ok(ApiResponseDTO.ok(inventarioService.listarPorSucursal(idSucursal)));
    }

    @GetMapping("/producto/{idProducto}/sucursal/{idSucursal}")
    @PreAuthorize("hasAnyRole('ADMIN','BODEGUERO')")
    @Operation(summary = "Consultar stock de un producto en una sucursal")
    public ResponseEntity<ApiResponseDTO<InventarioResponseDTO>> buscarStock(
            @PathVariable Integer idProducto, @PathVariable Integer idSucursal) {
        return ResponseEntity.ok(ApiResponseDTO.ok(
                inventarioService.buscarPorProductoYSucursal(idProducto, idSucursal)));
    }

    @GetMapping("/paginado")
    @PreAuthorize("hasAnyRole('ADMIN', 'BODEGUERO')")
    @Operation(summary = "Listar inventario paginado con búsqueda y filtros")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<InventarioResponseDTO>>> buscarPaginado(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponseDTO.ok(inventarioService.buscarPaginado(search, idSucursal, page, size)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BODEGUERO')")
    @Operation(summary = "Actualizar stock")
    public ResponseEntity<ApiResponseDTO<InventarioResponseDTO>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody InventarioRequestDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.ok("Stock actualizado", inventarioService.actualizarStock(dto)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BODEGUERO')")
    @Operation(summary = "Buscar inventario por ID")
    public ResponseEntity<ApiResponseDTO<InventarioResponseDTO>> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponseDTO.ok(inventarioService.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BODEGUERO')")
    @Operation(summary = "Crear registro de inventario")
    public ResponseEntity<ApiResponseDTO<InventarioResponseDTO>> crear(
            @Valid @RequestBody InventarioRequestDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.ok("Inventario creado", inventarioService.actualizarStock(dto)));
    }

    @PatchMapping("/ajustar")
    @PreAuthorize("hasAnyRole('ADMIN','BODEGUERO')")
    @Operation(summary = "Ajustar stock (suma o resta)")
    public ResponseEntity<ApiResponseDTO<Void>> ajustar(
            @RequestParam Integer idProducto,
            @RequestParam Integer idSucursal,
            @RequestParam Integer cantidad) {
        inventarioService.ajustarStock(idProducto, idSucursal, cantidad);
        return ResponseEntity.ok(ApiResponseDTO.ok("Stock ajustado", null));
    }
}
