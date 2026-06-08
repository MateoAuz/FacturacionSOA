package com.empresa.sistema.repository;

import com.empresa.sistema.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    List<Usuario> findByActivoTrue();
    List<Usuario> findBySucursal_IdSucursal(Integer idSucursal);
    @Query("SELECT u FROM Usuario u WHERE " +
            "(:search IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.correo) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:idRol IS NULL OR u.rol.idRol = :idRol)")
    Page<Usuario> buscarPaginado(@Param("search") String search,
                                 @Param("idRol") Integer idRol,
                                 Pageable pageable);
    List<Usuario> findByRol_NombreAndSucursal_IdSucursalAndActivoTrue(String rolNombre, Integer idSucursal);
}