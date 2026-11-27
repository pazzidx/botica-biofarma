package com.biofarma.inventario.domain.model;

/**
 * Representa la identidad y rol del usuario autenticado extraídos desde el token de sesión en el servidor.
 */
public record AuthenticatedUser(
        Integer idUsuario,
        String codigoUsuario,
        RolNombre rol
) {

    public boolean esAdministradora() {
        return rol == RolNombre.ADMINISTRADORA;
    }

    public boolean esTrabajadora() {
        return rol == RolNombre.TRABAJADORA;
    }
}
