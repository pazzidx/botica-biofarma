package com.biofarma.inventario.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO inmutable para la solicitud de inicio de sesión (HU-02 / BIO-30).
 */
public record LoginRequest(
        @NotBlank(message = "El código de usuario es obligatorio.")
        String codigoUsuario,

        @NotBlank(message = "La contraseña es obligatoria.")
        String password
) {
}
