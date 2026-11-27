package com.biofarma.inventario.infrastructure.security;

import com.biofarma.inventario.domain.model.RolNombre;
import java.security.Principal;

/**
 * Principal almacenado en el SecurityContext tras validar el token JWT.
 * Implementa {@link Principal} para que authentication.getName() devuelva codigoUsuario.
 */
public record UsuarioAutenticadoPrincipal(
        Integer idUsuario,
        String codigoUsuario,
        RolNombre rol
) implements Principal {

    @Override
    public String getName() {
        return codigoUsuario;
    }
}
