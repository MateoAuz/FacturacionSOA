package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Sucursal;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface SucursalRepository extends JpaRepository<Sucursal, Integer> {
    List<Sucursal> findByActivoTrue();

    /** Bloqueo pesimista de escritura — evita race conditions en numeración de facturas */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Sucursal s WHERE s.idSucursal = :id")
    Optional<Sucursal> findByIdWithLock(@Param("id") Integer id);
}
