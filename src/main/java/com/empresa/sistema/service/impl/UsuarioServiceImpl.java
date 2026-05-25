package com.empresa.sistema.service.impl;

import com.empresa.sistema.dto.request.UsuarioRequestDTO;
import com.empresa.sistema.dto.response.UsuarioResponseDTO;
import com.empresa.sistema.entity.Rol;
import com.empresa.sistema.entity.Sucursal;
import com.empresa.sistema.entity.Usuario;
import com.empresa.sistema.repository.RolRepository;
import com.empresa.sistema.repository.SucursalRepository;
import com.empresa.sistema.repository.UsuarioRepository;
import com.empresa.sistema.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import com.empresa.sistema.dto.response.PageResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SucursalRepository sucursalRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findByActivoTrue().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public UsuarioResponseDTO buscarPorId(Integer id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        return toDTO(u);
    }

    @Override
    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        Rol rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        Sucursal sucursal = sucursalRepository.findById(dto.getIdSucursal())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        Usuario u = Usuario.builder()
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .username(dto.getUsername())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .correo(dto.getCorreo())
                .rol(rol)
                .sucursal(sucursal)
                .activo(true)
                .build();
        return toDTO(usuarioRepository.save(u));
    }

    @Override
    public UsuarioResponseDTO actualizar(Integer id, UsuarioRequestDTO dto) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        Rol rol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        Sucursal sucursal = sucursalRepository.findById(dto.getIdSucursal())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        u.setNombre(dto.getNombre());
        u.setApellido(dto.getApellido());
        u.setCorreo(dto.getCorreo());
        u.setRol(rol);
        u.setSucursal(sucursal);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        return toDTO(usuarioRepository.save(u));
    }

    @Override
    public void eliminar(Integer id) {
        usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        usuarioRepository.deleteById(id);
    }

    @Override
    public void cambiarEstado(Integer id, Boolean activo) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + id));
        u.setActivo(activo);
        usuarioRepository.save(u);
    }

    @Override
    public PageResponseDTO<UsuarioResponseDTO> buscarPaginado(String search, Integer idRol, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<Usuario> resultado = usuarioRepository.buscarPaginado(
                (search != null && !search.isBlank()) ? search : null,
                idRol,
                pageable);
        return PageResponseDTO.<UsuarioResponseDTO>builder()
                .contenido(resultado.getContent().stream().map(this::toDTO).collect(Collectors.toList()))
                .paginaActual(resultado.getNumber())
                .totalPaginas(resultado.getTotalPages())
                .totalElementos(resultado.getTotalElements())
                .tamanioPagina(resultado.getSize())
                .primera(resultado.isFirst())
                .ultima(resultado.isLast())
                .build();
    }

    private UsuarioResponseDTO toDTO(Usuario u) {
        return UsuarioResponseDTO.builder()
                .idUsuario(u.getIdUsuario())
                .nombre(u.getNombre())
                .apellido(u.getApellido())
                .username(u.getUsername())
                .correo(u.getCorreo())
                .rol(u.getRol().getNombre())
                .sucursal(u.getSucursal().getNombre())
                .activo(u.getActivo())
                .fechaRegistro(u.getFechaRegistro())
                .build();
    }
}
