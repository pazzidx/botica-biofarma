package com.biofarma.inventario.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de entrada para inicio de sesión.
 */
public record LoginRequest(
        @NotBlank(message = "El código de usuario es obligatorio.")
        String codigoUsuario,

        @NotBlank(message = "La contraseña es obligatoria.")
        String password
) {
}
