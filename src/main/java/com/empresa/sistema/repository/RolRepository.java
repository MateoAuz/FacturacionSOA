package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Integer> {
    Optional<Rol> findByNombre(String nombre);
}
