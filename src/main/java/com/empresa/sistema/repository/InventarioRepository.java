package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventarioRepository extends JpaRepository<Inventario, Integer> {
    Optional<Inventario> findByProducto_IdProductoAndSucursal_IdSucursal(Integer idProducto, Integer idSucursal);
    List<Inventario> findBySucursal_IdSucursal(Integer idSucursal);
    List<Inventario> findByProducto_IdProducto(Integer idProducto);
    @Query("SELECT i FROM Inventario i WHERE " +
            "(:idSucursal IS NULL OR i.sucursal.idSucursal = :idSucursal) AND " +
            "(:search IS NULL OR LOWER(i.producto.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(i.producto.codigo) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Inventario> buscarPaginado(@Param("search") String search,
                                    @Param("idSucursal") Integer idSucursal,
                                    Pageable pageable);
}
