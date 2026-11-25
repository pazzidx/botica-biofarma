package com.biofarma.inventario.application.dto;

import java.time.LocalDateTime;

/**
 * Respuesta pública de usuario sin exponer contraseña ni hash (CA-06).
 */
public record UsuarioResponse(
        Integer idUsuario,
        String codigoUsuario,
        String nombres,
        String apellidos,
        String rol,
        boolean activo,
        LocalDateTime fechaCreacion
) {
}
