package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    Optional<Producto> findByCodigo(String codigo);
    List<Producto> findByActivoTrue();
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    List<Producto> findByCategoria_IdCategoriaAndActivoTrue(Integer idCategoria);
}
