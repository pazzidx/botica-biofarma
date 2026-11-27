package com.biofarma.inventario.application.dto;

import java.math.BigDecimal;

/**
 * DTO de respuesta para una línea de detalle de venta.
 */
public record DetalleVentaResponse(
        Integer idDetalle,
        Integer idLote,
        String nombreProducto,
        String numeroLote,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}
