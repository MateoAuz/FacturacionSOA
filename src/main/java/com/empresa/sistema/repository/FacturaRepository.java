package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FacturaRepository extends JpaRepository<Factura, Integer> {
    Optional<Factura> findByVenta_IdVenta(Integer idVenta);
    Optional<Factura> findByNumeroSecuencial(String numeroSecuencial);
}
