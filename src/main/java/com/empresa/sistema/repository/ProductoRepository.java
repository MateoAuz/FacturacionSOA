package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    Optional<Producto> findByCodigo(String codigo);
    List<Producto> findByActivoTrue();
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByCategoria_IdCategoriaAndActivoTrue(Integer idCategoria);

    @Query("SELECT p FROM Producto p WHERE p.activo = true AND " +
            "(:search IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:idCategoria IS NULL OR p.categoria.idCategoria = :idCategoria)")
    Page<Producto> buscarPaginado(@Param("search") String search,
                                  @Param("idCategoria") Integer idCategoria,
                                  Pageable pageable);

    /** Filtra solo productos con stock > 0 en la sucursal indicada */
    @Query(value = "SELECT p FROM Producto p " +
            "JOIN Inventario i ON i.producto = p " +
            "WHERE p.activo = true " +
            "AND i.sucursal.idSucursal = :idSucursal " +
            "AND i.cantidad > 0 " +
            "AND (:search IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "  OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :search, '%')))",
           countQuery = "SELECT COUNT(p) FROM Producto p " +
            "JOIN Inventario i ON i.producto = p " +
            "WHERE p.activo = true " +
            "AND i.sucursal.idSucursal = :idSucursal " +
            "AND i.cantidad > 0 " +
            "AND (:search IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "  OR LOWER(p.codigo) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Producto> buscarPaginadoConStock(@Param("search") String search,
                                          @Param("idSucursal") Integer idSucursal,
                                          Pageable pageable);
}