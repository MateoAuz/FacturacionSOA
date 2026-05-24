package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
    Optional<Venta> findByNumeroVenta(String numeroVenta);
    List<Venta> findBySucursal_IdSucursal(Integer idSucursal);
    List<Venta> findByUsuario_IdUsuario(Integer idUsuario);
    List<Venta> findByFechaVentaBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Venta> findByCliente_IdCliente(Integer idCliente);
}
