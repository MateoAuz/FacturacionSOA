package com.empresa.sistema.repository;

import com.empresa.sistema.entity.SolicitudStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SolicitudStockRepository extends JpaRepository<SolicitudStock, Integer> {

    /** Solicitudes pendientes que deben atender los bodegueros de una sucursal proveedora */
    List<SolicitudStock> findBySucursalProveedora_IdSucursalAndEstado(
            Integer idSucursal, SolicitudStock.Estado estado);

    /** Solicitudes creadas por los cajeros de una sucursal solicitante */
    List<SolicitudStock> findBySucursalSolicitante_IdSucursalOrderByFechaSolicitudDesc(
            Integer idSucursal);

    /** Cuenta pendientes para badge de bodeguero */
    @Query("SELECT COUNT(s) FROM SolicitudStock s WHERE s.sucursalProveedora.idSucursal = :idSucursal AND s.estado = 'PENDIENTE'")
    long countPendientesByProveedora(@Param("idSucursal") Integer idSucursal);

    /** Suma de cantidades pendientes para un producto desde una sucursal proveedora */
    @Query("SELECT COALESCE(SUM(s.cantidad), 0) FROM SolicitudStock s " +
           "WHERE s.producto.idProducto = :idProducto " +
           "AND s.sucursalProveedora.idSucursal = :idSucursal " +
           "AND s.estado = 'PENDIENTE'")
    int sumCantidadPendienteByProductoAndProveedora(
            @Param("idProducto") Integer idProducto,
            @Param("idSucursal") Integer idSucursal);

    /** ¿Ya existe una solicitud pendiente del mismo cajero para el mismo producto/proveedora? */
    boolean existsByProducto_IdProductoAndSucursalSolicitante_IdSucursalAndSucursalProveedora_IdSucursalAndEstado(
            Integer idProducto, Integer idSucursalSolicitante,
            Integer idSucursalProveedora, SolicitudStock.Estado estado);
}
