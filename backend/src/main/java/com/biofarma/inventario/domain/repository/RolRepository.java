package com.biofarma.inventario.domain.repository;

import com.biofarma.inventario.domain.model.Rol;
import com.biofarma.inventario.domain.model.RolNombre;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para acceso a roles en persistencia.
 */
public interface RolRepository {

    Optional<Rol> findByNombre(RolNombre nombre);

    List<Rol> findAll();
}
