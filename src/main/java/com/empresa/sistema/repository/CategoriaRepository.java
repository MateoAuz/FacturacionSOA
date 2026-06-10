package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    List<Categoria> findByActivoTrue();
    boolean existsByNombre(String nombre);
}
