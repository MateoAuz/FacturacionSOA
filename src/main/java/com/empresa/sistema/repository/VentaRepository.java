package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
    Optional<Venta> findByNumeroVenta(String numeroVenta);
    List<Venta> findBySucursal_IdSucursal(Integer idSucursal);
    List<Venta> findByUsuario_IdUsuario(Integer idUsuario);
    List<Venta> findByFechaVentaBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Venta> findByCliente_IdCliente(Integer idCliente);

    @Query("SELECT v FROM Venta v WHERE " +
            "(:idSucursal IS NULL OR v.sucursal.idSucursal = :idSucursal) AND " +
            "(:estado IS NULL OR v.estado = :estado) AND " +
            "(:search IS NULL OR LOWER(v.numeroVenta) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.cliente.nombres) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Venta> buscarPaginado(@Param("search") String search,
                               @Param("idSucursal") Integer idSucursal,
                               @Param("estado") Venta.EstadoVenta estado,
                               Pageable pageable);
}

