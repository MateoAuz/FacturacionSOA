package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByIdentificacion(String identificacion);
    List<Cliente> findByActivoTrue();
    List<Cliente> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(String nombres, String apellidos);
}
