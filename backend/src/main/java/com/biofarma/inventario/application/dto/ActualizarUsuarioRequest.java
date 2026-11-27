package com.biofarma.inventario.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Solicitud inmutable para editar únicamente nombres y apellidos de una trabajadora (HU-01).
 */
public record ActualizarUsuarioRequest(
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
        String apellidos
) {
}
