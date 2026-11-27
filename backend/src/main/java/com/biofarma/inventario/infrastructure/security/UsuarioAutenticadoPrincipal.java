package com.biofarma.inventario.infrastructure.security;

import com.biofarma.inventario.domain.model.RolNombre;

/**
 * Principal almacenado en el SecurityContext tras validar el token JWT.
 */
public record UsuarioAutenticadoPrincipal(
        Integer idUsuario,
        String codigoUsuario,
        RolNombre rol
) {
}
