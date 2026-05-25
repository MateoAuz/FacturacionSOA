package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FacturaRepository extends JpaRepository<Factura, Integer> {
    Optional<Factura> findByVenta_IdVenta(Integer idVenta);
    Optional<Factura> findByNumeroSecuencial(String numeroSecuencial);
    @Query("SELECT f FROM Factura f " +
            "JOIN FETCH f.venta v " +
            "JOIN FETCH v.cliente " +
            "JOIN FETCH v.sucursal " +
            "WHERE (:estado IS NULL OR f.estado = :estado) AND " +
            "(:search IS NULL OR LOWER(f.numeroSecuencial) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.cliente.nombres) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Factura> buscarPaginado(@Param("search") String search,
                                 @Param("estado") Factura.EstadoFactura estado,
                                 Pageable pageable);
}

