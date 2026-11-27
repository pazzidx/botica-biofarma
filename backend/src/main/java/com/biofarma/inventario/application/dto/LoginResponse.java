package com.biofarma.inventario.application.dto;

/**
 * DTO inmutable de respuesta para el inicio de sesión con JWT.
 */
public record LoginResponse(
        String token,
        UsuarioAutenticadoDTO usuario
) {
    public record UsuarioAutenticadoDTO(
            Integer idUsuario,
            String codigoUsuario,
            String nombreCompleto,
            String rol
    ) {
    }
}
