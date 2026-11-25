package com.biofarma.inventario.presentation.advice;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Formato estándar de respuestas de error HTTP sin exponer detalles internos ni información sensible.
 */
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
