package com.biofarma.inventario.infrastructure.persistence.repository;

import com.biofarma.inventario.infrastructure.persistence.entity.RolEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para la tabla Roles.
 */
public interface SpringDataRolRepository extends JpaRepository<RolEntity, Integer> {

    Optional<RolEntity> findByNombre(String nombre);
}
