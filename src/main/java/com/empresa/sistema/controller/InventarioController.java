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

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN','BODEGUERO')")
    @Operation(summary = "Actualizar stock")
    public ResponseEntity<ApiResponseDTO<InventarioResponseDTO>> actualizar(
            @Valid @RequestBody InventarioRequestDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.ok("Stock actualizado", inventarioService.actualizarStock(dto)));
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
