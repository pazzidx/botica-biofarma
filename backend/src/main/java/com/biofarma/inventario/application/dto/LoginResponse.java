package com.biofarma.inventario.application.dto;

/**
 * DTO inmutable con la respuesta de autenticación exitosa (HU-02 / BIO-30).
 * Contiene el token JWT, vigencia y datos públicos del usuario.
 * NUNCA expone contraseñas ni hashes.
 */
public record LoginResponse(
        String token,
        String tipoToken,
        long expiraEnMs,
        Integer idUsuario,
        String codigoUsuario,
        String nombres,
        String apellidos,
        String rol
) {
}
