package com.biofarma.inventario.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Solicitud inmutable para registrar un nuevo usuario (HU-01).
 */
public record RegistrarUsuarioRequest(
        @NotBlank(message = "El código de usuario es obligatorio.")
        @Size(min = 3, max = 30, message = "El código de usuario debe tener entre 3 y 30 caracteres.")
        @Pattern(
                regexp = "^[A-Za-z0-9._-]+$",
                message = "El código de usuario solo puede contener letras, números, puntos, guiones y guion bajo."
        )
        String codigoUsuario,

        @NotBlank(message = "Los nombres son obligatorios.")
        @Size(max = 100, message = "Los nombres no pueden exceder 100 caracteres.")
        @Pattern(
                regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$",
                message = "Los nombres solo pueden contener letras, tildes, ñ y espacios."
        )
        String nombres,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(max = 100, message = "Los apellidos no pueden exceder 100 caracteres.")
        @Pattern(
                regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$",
                message = "Los apellidos solo pueden contener letras, tildes, ñ y espacios."
        )
        String apellidos,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres.")
        String password,

        String rol
) {
}
