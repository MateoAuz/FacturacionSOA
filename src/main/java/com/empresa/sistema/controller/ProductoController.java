package com.empresa.sistema.controller;

import com.empresa.sistema.dto.request.ProductoRequestDTO;
import com.empresa.sistema.dto.response.ApiResponseDTO;
import com.empresa.sistema.dto.response.ProductoResponseDTO;
import com.empresa.sistema.service.ProductoService;
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
@RequestMapping("/api/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión de productos")
@SecurityRequirement(name = "bearerAuth")
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar todos los productos activos")
    public ResponseEntity<ApiResponseDTO<List<ProductoResponseDTO>>> listar() {
        return ResponseEntity.ok(ApiResponseDTO.ok(productoService.listarTodos()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<ApiResponseDTO<ProductoResponseDTO>> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponseDTO.ok(productoService.buscarPorId(id)));
    }

    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Buscar producto por código")
    public ResponseEntity<ApiResponseDTO<ProductoResponseDTO>> buscarPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(ApiResponseDTO.ok(productoService.buscarPorCodigo(codigo)));
    }

    @GetMapping("/buscar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Buscar productos por nombre")
    public ResponseEntity<ApiResponseDTO<List<ProductoResponseDTO>>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(ApiResponseDTO.ok(productoService.buscarPorNombre(nombre)));
    }

    @GetMapping("/categoria/{idCategoria}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar productos por categoría")
    public ResponseEntity<ApiResponseDTO<List<ProductoResponseDTO>>> buscarPorCategoria(@PathVariable Integer idCategoria) {
        return ResponseEntity.ok(ApiResponseDTO.ok(productoService.buscarPorCategoria(idCategoria)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BODEGUERO')")
    @Operation(summary = "Crear nuevo producto")
    public ResponseEntity<ApiResponseDTO<ProductoResponseDTO>> crear(@Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Producto creado", productoService.crear(dto)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BODEGUERO')")
    @Operation(summary = "Actualizar producto")
    public ResponseEntity<ApiResponseDTO<ProductoResponseDTO>> actualizar(
            @PathVariable Integer id, @Valid @RequestBody ProductoRequestDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.ok("Producto actualizado", productoService.actualizar(id, dto)));
    }
    @GetMapping("/paginado")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar productos paginado con búsqueda y filtros")
    public ResponseEntity<ApiResponseDTO<PageResponseDTO<ProductoResponseDTO>>> buscarPaginado(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String campo,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false) Integer idSucursal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponseDTO.ok(
                productoService.buscarPaginado(search, campo, idCategoria, idSucursal, page, size)));
    }



    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar producto")
    public ResponseEntity<ApiResponseDTO<Void>> eliminar(@PathVariable Integer id) {
        productoService.eliminar(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Producto eliminado", null));
    }

    @PatchMapping("/{id}/toggle-activo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar o desactivar producto")
    public ResponseEntity<ApiResponseDTO<ProductoResponseDTO>> toggleActivo(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponseDTO.ok(productoService.toggleActivo(id)));
    }
}
