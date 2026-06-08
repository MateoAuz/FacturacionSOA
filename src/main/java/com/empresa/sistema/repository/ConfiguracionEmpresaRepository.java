package com.empresa.sistema.repository;

import com.empresa.sistema.entity.ConfiguracionEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionEmpresaRepository extends JpaRepository<ConfiguracionEmpresa, Integer> {
    /** Devuelve la primera fila — la empresa siempre tiene exactamente 1 registro. */
    Optional<ConfiguracionEmpresa> findFirstBy();
}
