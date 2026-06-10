package com.empresa.sistema.util;

import com.empresa.sistema.entity.Rol;
import com.empresa.sistema.entity.Sucursal;
import com.empresa.sistema.entity.Usuario;
import com.empresa.sistema.repository.RolRepository;
import com.empresa.sistema.repository.SucursalRepository;
import com.empresa.sistema.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el usuario admin por defecto si no existe ningún usuario en la BD.
 * Solo se ejecuta una vez en el primer arranque.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SucursalRepository sucursalRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Rol rolAdmin = rolRepository.findByNombre("ADMIN")
                    .orElseThrow(() -> new RuntimeException(
                            "Rol ADMIN no encontrado. Verifica que schema.sql fue ejecutado."));

            Sucursal sucursalPrincipal = sucursalRepository.findAll().stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "No hay sucursales. Verifica que schema.sql fue ejecutado."));

            Usuario admin = Usuario.builder()
                    .nombre("Administrador")
                    .apellido("Sistema")
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("Admin1234"))
                    .correo("admin@empresa.com")
                    .rol(rolAdmin)
                    .sucursal(sucursalPrincipal)
                    .activo(true)
                    .build();

            usuarioRepository.save(admin);
            log.info("=======================================================");
            log.info("  Usuario admin creado:");
            log.info("  Username : admin");
            log.info("  Password : Admin1234");
            log.info("  CAMBIA LA CONTRASEÑA despues del primer login.");
            log.info("=======================================================");
        }
    }
}
