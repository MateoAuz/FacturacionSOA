package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    List<Usuario> findByActivoTrue();
    List<Usuario> findBySucursal_IdSucursal(Integer idSucursal);
}
