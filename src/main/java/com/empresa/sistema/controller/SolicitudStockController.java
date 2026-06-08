package com.empresa.sistema.controller;

import com.empresa.sistema.dto.request.SolicitudStockRequestDTO;
import com.empresa.sistema.dto.response.ApiResponseDTO;
import com.empresa.sistema.dto.response.SolicitudStockResponseDTO;
import com.empresa.sistema.service.SolicitudStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes-stock")
@RequiredArgsConstructor
@Tag(name = "Solicitudes de Stock", description = "Transferencias inter-sucursal")
@SecurityRequirement(name = "bearerAuth")
public class SolicitudStockController {

    private final SolicitudStockService solicitudService;

    /** CAJERO crea solicitud */
    @PostMapping
    @PreAuthorize("hasAnyRole('CAJERO','ADMIN')")
    @Operation(summary = "Crear solicitud de transferencia de stock")
    public ResponseEntity<ApiResponseDTO<SolicitudStockResponseDTO>> crear(
            @Valid @RequestBody SolicitudStockRequestDTO dto,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponseDTO.ok("Solicitud enviada",
                solicitudService.crear(dto, user.getUsername())));
    }

    /** BODEGUERO ve sus pendientes */
    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('BODEGUERO','ADMIN')")
    @Operation(summary = "Listar solicitudes pendientes para el bodeguero autenticado")
    public ResponseEntity<ApiResponseDTO<List<SolicitudStockResponseDTO>>> pendientes(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponseDTO.ok(solicitudService.listarPendientes(user.getUsername())));
    }

    /** Badge counter de pendientes */
    @GetMapping("/pendientes/count")
    @PreAuthorize("hasAnyRole('BODEGUERO','ADMIN')")
    @Operation(summary = "Contar solicitudes pendientes (badge)")
    public ResponseEntity<ApiResponseDTO<Long>> countPendientes(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponseDTO.ok(solicitudService.contarPendientes(user.getUsername())));
    }

    /** BODEGUERO acepta */
    @PatchMapping("/{id}/aceptar")
    @PreAuthorize("hasAnyRole('BODEGUERO','ADMIN')")
    @Operation(summary = "Aceptar solicitud y transferir stock")
    public ResponseEntity<ApiResponseDTO<SolicitudStockResponseDTO>> aceptar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponseDTO.ok("Solicitud aceptada. Stock transferido.",
                solicitudService.aceptar(id, user.getUsername())));
    }

    /** BODEGUERO rechaza */
    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('BODEGUERO','ADMIN')")
    @Operation(summary = "Rechazar solicitud")
    public ResponseEntity<ApiResponseDTO<SolicitudStockResponseDTO>> rechazar(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(ApiResponseDTO.ok("Solicitud rechazada.",
                solicitudService.rechazar(id, user.getUsername())));
    }
}
