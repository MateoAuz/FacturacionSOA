package com.empresa.sistema.controller;

import com.empresa.sistema.dto.request.ClienteRequestDTO;
import com.empresa.sistema.dto.response.ApiResponseDTO;
import com.empresa.sistema.dto.response.ClienteResponseDTO;
import com.empresa.sistema.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.empresa.sistema.dto.response.PageResponseDTO;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de clientes")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Listar todos los clientes")
    public ResponseEntity<ApiResponseDTO<List<ClienteResponseDTO>>> listar() {
        return ResponseEntity.ok(ApiResponseDTO.ok(clienteService.listarTodos()));
    }
    @GetMapping("/paginado")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar clientes paginado con búsqueda")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ClienteResponseDTO>>> buscarPaginado(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String campo,
            @RequestParam(required = false) String tipo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponseDTO.ok(clienteService.buscarPaginado(search, campo, tipo, page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Obtener cliente por ID")
    public ResponseEntity<ApiResponseDTO<ClienteResponseDTO>> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponseDTO.ok(clienteService.buscarPorId(id)));
    }

    @GetMapping("/identificacion/{identificacion}")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Buscar cliente por identificación")
    public ResponseEntity<ApiResponseDTO<ClienteResponseDTO>> buscarPorIdentificacion(
            @PathVariable String identificacion) {
        return ResponseEntity.ok(ApiResponseDTO.ok(clienteService.buscarPorIdentificacion(identificacion)));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Buscar clientes por nombre")
    public ResponseEntity<ApiResponseDTO<List<ClienteResponseDTO>>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(ApiResponseDTO.ok(clienteService.buscarPorNombre(nombre)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Crear nuevo cliente")
    public ResponseEntity<ApiResponseDTO<ClienteResponseDTO>> crear(@Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Cliente creado", clienteService.crear(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CAJERO')")
    @Operation(summary = "Actualizar cliente")
    public ResponseEntity<ApiResponseDTO<ClienteResponseDTO>> actualizar(
            @PathVariable Integer id, @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.ok("Cliente actualizado", clienteService.actualizar(id, dto)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar estado activo/inactivo de cliente")
    public ResponseEntity<ApiResponseDTO<Void>> cambiarEstado(
            @PathVariable Integer id, @RequestParam Boolean activo) {
        clienteService.cambiarEstado(id, activo);
        return ResponseEntity.ok(ApiResponseDTO.ok("Estado actualizado", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar cliente")
    public ResponseEntity<ApiResponseDTO<Void>> eliminar(@PathVariable Integer id) {
        clienteService.eliminar(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Cliente eliminado", null));
    }
}
