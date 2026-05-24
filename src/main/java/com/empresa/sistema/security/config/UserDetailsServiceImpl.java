package com.empresa.sistema.security.config;

import com.empresa.sistema.entity.Usuario;
import com.empresa.sistema.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre())
                ))
                .build();
    }
}
