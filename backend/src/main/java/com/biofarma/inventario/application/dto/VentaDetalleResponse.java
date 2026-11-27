package com.biofarma.inventario.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de respuesta completa para el detalle de una venta individual.
 */
public record VentaDetalleResponse(
        Integer idVenta,
        String numeroTicket,
        Integer idUsuario,
        String codigoUsuario,
        String nombreUsuario,
        LocalDateTime fechaVenta,
        BigDecimal total,
        String estado,
        List<DetalleVentaResponse> detalles
) {
}
