package com.biofarma.inventario.domain.model;

import com.biofarma.inventario.domain.exception.DatosUsuarioInvalidosException;
import java.util.Objects;

/**
 * Entidad de dominio que representa un rol del personal en Botica Biofarma.
 */
public class Rol {

    private final Integer idRol;
    private final RolNombre nombre;

    public Rol(Integer idRol, RolNombre nombre) {
        if (nombre == null) {
            throw new DatosUsuarioInvalidosException("El nombre del rol es obligatorio.");
        }
        this.idRol = idRol;
        this.nombre = nombre;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public RolNombre getNombre() {
        return nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rol rol)) return false;
        return Objects.equals(idRol, rol.idRol) && nombre == rol.nombre;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRol, nombre);
    }
}
