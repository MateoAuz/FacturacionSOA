package com.empresa.sistema.controller;

import com.empresa.sistema.dto.response.ApiResponseDTO;
import com.empresa.sistema.entity.Rol;
import com.empresa.sistema.repository.RolRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Gestión de roles")
@SecurityRequirement(name = "bearerAuth")
public class RolController {

    private final RolRepository rolRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponseDTO<List<Rol>>> listar() {
        return ResponseEntity.ok(ApiResponseDTO.ok(rolRepository.findAll()));
    }
}