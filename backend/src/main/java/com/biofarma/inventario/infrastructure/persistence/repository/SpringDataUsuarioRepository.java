package com.biofarma.inventario.infrastructure.persistence.repository;

import com.biofarma.inventario.infrastructure.persistence.entity.UsuarioEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio Spring Data JPA para la tabla usuarios.
 */
public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {

    boolean existsByCodigoUsuarioIgnoreCase(String codigoUsuario);

    Optional<UsuarioEntity> findByCodigoUsuarioIgnoreCase(String codigoUsuario);

    List<UsuarioEntity> findAllByOrderByFechaCreacionDesc();

    @Query(value = "SELECT COUNT(*) FROM ventas WHERE id_usuario = :id", nativeQuery = true)
    long countVentasByUsuarioId(@Param("id") Integer idUsuario);
}
