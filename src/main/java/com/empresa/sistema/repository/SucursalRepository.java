package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SucursalRepository extends JpaRepository<Sucursal, Integer> {
    List<Sucursal> findByActivoTrue();
}
