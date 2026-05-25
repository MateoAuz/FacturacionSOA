package com.empresa.sistema.controller;

import com.empresa.sistema.dto.request.LoginRequestDTO;
import com.empresa.sistema.dto.response.ApiResponseDTO;
import com.empresa.sistema.dto.response.LoginResponseDTO;
import com.empresa.sistema.entity.Usuario;
import com.empresa.sistema.repository.UsuarioRepository;
import com.empresa.sistema.security.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de autenticación JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener token JWT")
    public ResponseEntity<ApiResponseDTO<LoginResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = jwtUtil.generarToken(userDetails);
        String rol = userDetails.getAuthorities().stream()
                .findFirst().map(a -> a.getAuthority().replace("ROLE_", "")).orElse("");

        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername()).orElse(null);
        Integer idSucursal = usuario != null && usuario.getSucursal() != null ? usuario.getSucursal().getIdSucursal() : null;
        String sucursal = usuario != null && usuario.getSucursal() != null ? usuario.getSucursal().getNombre() : null;

        LoginResponseDTO response = LoginResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .username(userDetails.getUsername())
                .rol(rol)
                .expiracion(jwtUtil.getExpiration())
                .idSucursal(idSucursal)
                .sucursal(sucursal)
                .build();
        return ResponseEntity.ok(ApiResponseDTO.ok("Login exitoso", response));
    }
}