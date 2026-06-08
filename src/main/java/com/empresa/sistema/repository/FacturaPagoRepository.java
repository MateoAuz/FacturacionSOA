package com.empresa.sistema.repository;

import com.empresa.sistema.entity.FacturaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FacturaPagoRepository extends JpaRepository<FacturaPago, Integer> {
    List<FacturaPago> findByFactura_IdFacturaOrderByIdPagoAsc(Integer idFactura);
}
