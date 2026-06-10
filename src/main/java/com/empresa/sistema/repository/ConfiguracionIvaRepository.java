package com.empresa.sistema.repository;

import com.empresa.sistema.entity.ConfiguracionIva;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConfiguracionIvaRepository extends JpaRepository<ConfiguracionIva, Integer> {
    Optional<ConfiguracionIva> findByActivoTrue();
}
