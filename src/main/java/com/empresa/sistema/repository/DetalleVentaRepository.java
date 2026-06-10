package com.empresa.sistema.repository;

import com.empresa.sistema.entity.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Integer> {
    List<DetalleVenta> findByFactura_IdFactura(Integer idFactura);
}
