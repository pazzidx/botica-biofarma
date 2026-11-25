package com.biofarma.inventario.domain.model;

import com.biofarma.inventario.domain.exception.DatosUsuarioInvalidosException;
import java.util.Locale;

/**
 * Roles autorizados del sistema Botica Biofarma.
 */
public enum RolNombre {
    ADMINISTRADORA,
    TRABAJADORA;

    /**
     * Convierte una cadena al rol de dominio correspondiente.
     *
     * @param valor nombre del rol recibido
     * @return valor del enum {@link RolNombre}
     */
    public static RolNombre fromString(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new DatosUsuarioInvalidosException("El rol del usuario es obligatorio.");
        }
        String normalizado = valor.trim().toUpperCase(Locale.ROOT);
        for (RolNombre rol : values()) {
            if (rol.name().equals(normalizado)) {
                return rol;
            }
        }
        throw new DatosUsuarioInvalidosException(
                "Rol no válido: '" + valor.trim() + "'. Roles permitidos: ADMINISTRADORA, TRABAJADORA."
        );
    }
}
