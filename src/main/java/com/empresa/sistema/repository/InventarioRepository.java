package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface InventarioRepository extends JpaRepository<Inventario, Integer> {
    Optional<Inventario> findByProducto_IdProductoAndSucursal_IdSucursal(Integer idProducto, Integer idSucursal);
    List<Inventario> findBySucursal_IdSucursal(Integer idSucursal);
    List<Inventario> findByProducto_IdProducto(Integer idProducto);
}
