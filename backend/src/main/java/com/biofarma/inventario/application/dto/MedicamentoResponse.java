package com.biofarma.inventario.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de salida con la información comercial de un medicamento del catálogo.
 */
public record MedicamentoResponse(
        Integer idProducto,
        String codigoProducto,
        String nombreComercial,
        String principioActivo,
        String presentacion,
        String laboratorio,
        BigDecimal precioVenta,
        int stockMinimo,
        boolean requiereReceta,
        boolean activo,
        LocalDateTime fechaRegistro
) {
}
