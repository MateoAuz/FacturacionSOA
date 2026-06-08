package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface FacturaRepository extends JpaRepository<Factura, Integer> {

    Optional<Factura> findByNumeroSecuencial(String numeroSecuencial);
    List<Factura> findBySucursal_IdSucursal(Integer idSucursal);
    long countBySucursal_IdSucursal(Integer idSucursal);
    List<Factura> findByCliente_IdCliente(Integer idCliente);

    @Query("SELECT f FROM Factura f " +
           "JOIN FETCH f.cliente " +
           "JOIN FETCH f.sucursal " +
           "WHERE (:estado IS NULL OR f.estado = :estado) AND " +
           "(:idSucursal IS NULL OR f.sucursal.idSucursal = :idSucursal) AND " +
           "(:search IS NULL OR LOWER(f.numeroSecuencial) LIKE LOWER(CONCAT('%',:search,'%')) OR " +
           "LOWER(f.cliente.nombres) LIKE LOWER(CONCAT('%',:search,'%')))")
    Page<Factura> buscarPaginado(@Param("search")     String search,
                                 @Param("estado")     Factura.EstadoFactura estado,
                                 @Param("idSucursal") Integer idSucursal,
                                 Pageable pageable);
}
