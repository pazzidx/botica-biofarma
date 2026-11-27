package com.biofarma.inventario.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta resumida para el listado del historial de ventas.
 */
public record VentaResumenResponse(
        Integer idVenta,
        String numeroTicket,
        Integer idUsuario,
        String codigoUsuario,
        String nombreUsuario,
        LocalDateTime fechaVenta,
        BigDecimal total,
        String estado,
        int cantidadItems
) {
}
