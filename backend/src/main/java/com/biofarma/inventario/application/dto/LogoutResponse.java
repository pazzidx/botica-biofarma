package com.biofarma.inventario.application.dto;

import java.time.LocalDateTime;

/**
 * DTO inmutable de respuesta al cierre de sesión (HU-02 / BIO-30).
 */
public record LogoutResponse(
        String mensaje,
        LocalDateTime timestamp
) {
}
