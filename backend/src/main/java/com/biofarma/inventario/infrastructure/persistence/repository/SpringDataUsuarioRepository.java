package com.biofarma.inventario.infrastructure.persistence.repository;

import com.biofarma.inventario.infrastructure.persistence.entity.UsuarioEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para la tabla Usuarios.
 */
public interface SpringDataUsuarioRepository extends JpaRepository<UsuarioEntity, Integer> {

    boolean existsByCodigoUsuarioIgnoreCase(String codigoUsuario);

    Optional<UsuarioEntity> findByCodigoUsuarioIgnoreCase(String codigoUsuario);

    List<UsuarioEntity> findAllByOrderByFechaCreacionDesc();
}
