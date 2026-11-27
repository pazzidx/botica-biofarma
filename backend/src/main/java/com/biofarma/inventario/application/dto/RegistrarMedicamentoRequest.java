package com.biofarma.inventario.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * DTO de entrada para registrar un nuevo medicamento en el catálogo (HU-04).
 */
public record RegistrarMedicamentoRequest(
        @NotBlank(message = "El código del medicamento es obligatorio.")
        @Size(min = 2, max = 30, message = "El código debe tener entre 2 y 30 caracteres.")
        String codigoProducto,

        @NotBlank(message = "El nombre comercial es obligatorio.")
        @Size(max = 150, message = "El nombre comercial no puede superar los 150 caracteres.")
        String nombreComercial,

        @NotBlank(message = "El principio activo es obligatorio.")
        @Size(max = 150, message = "El principio activo no puede superar los 150 caracteres.")
        String principioActivo,

        @NotBlank(message = "La presentación es obligatoria.")
        @Size(max = 80, message = "La presentación no puede superar los 80 caracteres.")
        String presentacion,

        @Size(max = 100, message = "El laboratorio no puede superar los 100 caracteres.")
        String laboratorio,

        @NotNull(message = "El precio unitario de venta es obligatorio.")
        @DecimalMin(value = "0.01", message = "El precio unitario de venta debe ser mayor a cero.")
        BigDecimal precioVenta,

        @NotNull(message = "El stock mínimo es obligatorio.")
        @Min(value = 0, message = "El stock mínimo no puede ser negativo.")
        Integer stockMinimo,

        Boolean requiereReceta
) {
}
