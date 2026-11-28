package com.biofarma.inventario.application.dto;

import java.math.BigDecimal;

/**
 * DTO de respuesta para consulta de disponibilidad y stock de medicamentos (HU-05).
 */
public record MedicamentoDisponibilidadResponse(
        Integer idProducto,
        String codigoProducto,
        String nombreComercial,
        String principioActivo,
        String presentacion,
        String laboratorio,
        BigDecimal precioVenta,
        int stockActual,
        boolean requiereReceta,
        String estadoDisponibilidad,
        boolean disponible
) {
}
