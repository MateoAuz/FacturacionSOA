package com.empresa.sistema.controller;

import com.empresa.sistema.dto.request.UsuarioRequestDTO;
import com.empresa.sistema.dto.response.ApiResponseDTO;
import com.empresa.sistema.dto.response.UsuarioResponseDTO;
import com.empresa.sistema.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.empresa.sistema.dto.response.PageResponseDTO;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los usuarios")
    public ResponseEntity<ApiResponseDTO<List<UsuarioResponseDTO>>> listar() {
        return ResponseEntity.ok(ApiResponseDTO.ok(usuarioService.listarTodos()));
    }

    @GetMapping("/paginado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios paginado con búsqueda y filtros")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<UsuarioResponseDTO>>> buscarPaginado(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer idRol,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponseDTO.ok(usuarioService.buscarPaginado(search, idRol, page, size)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<ApiResponseDTO<UsuarioResponseDTO>> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponseDTO.ok(usuarioService.buscarPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear nuevo usuario")
    public ResponseEntity<ApiResponseDTO<UsuarioResponseDTO>> crear(@Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Usuario creado", usuarioService.crear(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<ApiResponseDTO<UsuarioResponseDTO>> actualizar(
            @PathVariable Integer id, @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.ok("Usuario actualizado", usuarioService.actualizar(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<ApiResponseDTO<Void>> eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Usuario eliminado", null));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar estado activo/inactivo")
    public ResponseEntity<ApiResponseDTO<Void>> cambiarEstado(
            @PathVariable Integer id, @RequestParam Boolean activo) {
        usuarioService.cambiarEstado(id, activo);
        return ResponseEntity.ok(ApiResponseDTO.ok("Estado actualizado", null));
    }
}
