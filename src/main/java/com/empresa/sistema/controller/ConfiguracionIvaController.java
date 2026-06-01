package com.empresa.sistema.controller;

import com.empresa.sistema.dto.response.ApiResponseDTO;
import com.empresa.sistema.repository.ConfiguracionIvaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/configuracion-iva")
@RequiredArgsConstructor
@Tag(name = "Configuración IVA", description = "Configuración del IVA vigente")
@SecurityRequirement(name = "bearerAuth")
public class ConfiguracionIvaController {

    private final ConfiguracionIvaRepository configuracionIvaRepository;

    @GetMapping("/activa")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener configuración de IVA activa")
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getActiva() {
        Map<String, Object> datos;
        var ivaOpt = configuracionIvaRepository.findByActivoTrue();
        if (ivaOpt.isPresent()) {
            var iva = ivaOpt.get();
            datos = Map.of(
                "idIva",        (Object) iva.getIdIva(),
                "porcentaje",   iva.getPorcentaje(),
                "vigenciaDesde", iva.getVigenciaDesde().toString(),
                "activo",       iva.getActivo()
            );
        } else {
            datos = Map.of("porcentaje", (Object) java.math.BigDecimal.valueOf(15));
        }
        return ResponseEntity.ok(ApiResponseDTO.ok(datos));
    }
}
