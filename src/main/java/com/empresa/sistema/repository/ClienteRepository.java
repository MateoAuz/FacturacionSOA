package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
    Optional<Cliente> findByIdentificacion(String identificacion);
    List<Cliente> findByActivoTrue();
    List<Cliente> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(String nombres, String apellidos);

    @Query("SELECT c FROM Cliente c WHERE " +
            "(:search IS NULL OR " +
            "   (:campo IS NULL AND (LOWER(c.nombres) LIKE LOWER(CONCAT('%',:search,'%')) OR LOWER(c.apellidos) LIKE LOWER(CONCAT('%',:search,'%')) OR LOWER(c.identificacion) LIKE LOWER(CONCAT('%',:search,'%')) OR LOWER(c.correo) LIKE LOWER(CONCAT('%',:search,'%')))) OR " +
            "   (:campo = 'nombre' AND (LOWER(c.nombres) LIKE LOWER(CONCAT('%',:search,'%')) OR LOWER(c.apellidos) LIKE LOWER(CONCAT('%',:search,'%')))) OR " +
            "   (:campo = 'identificacion' AND LOWER(c.identificacion) LIKE LOWER(CONCAT('%',:search,'%'))) OR " +
            "   (:campo = 'correo' AND LOWER(c.correo) LIKE LOWER(CONCAT('%',:search,'%')))) AND " +
            "(:tipo IS NULL OR c.tipoIdentificacion = :tipo)")
    Page<Cliente> buscarPaginado(@Param("search") String search,
                                 @Param("campo") String campo,
                                 @Param("tipo") Cliente.TipoIdentificacion tipo,
                                 Pageable pageable);
}