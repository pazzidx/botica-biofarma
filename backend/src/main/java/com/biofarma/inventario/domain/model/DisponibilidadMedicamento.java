package com.biofarma.inventario.domain.model;

import com.biofarma.inventario.domain.exception.DatosInventarioInconsistentesException;
import java.math.BigDecimal;

/**
 * Modelo de dominio de solo lectura que combina la información comercial de un medicamento activo
 * con su stock vigente en inventario y calcula su estado de disponibilidad (HU-05).
 */
public record DisponibilidadMedicamento(
        Integer idProducto,
        String codigoProducto,
        String nombreComercial,
        String principioActivo,
        String presentacion,
        String laboratorio,
        BigDecimal precioVenta,
        int stockActual,
        boolean requiereReceta,
        EstadoDisponibilidad estadoDisponibilidad
) {

    /**
     * Evalúa y construye la disponibilidad de un medicamento activo validando invariantes
     * defensivas sobre precio, estado activo y consistencia del stock.
     */
    public static DisponibilidadMedicamento evaluar(Medicamento medicamento, Integer stockActual) {
        if (medicamento == null || medicamento.getIdProducto() == null) {
            throw new DatosInventarioInconsistentesException(
                    "No se pudo evaluar la disponibilidad de un medicamento inválido."
            );
        }
        if (!medicamento.isActivo()) {
            throw new DatosInventarioInconsistentesException(
                    "Los medicamentos inactivos no pueden mostrarse en la consulta de disponibilidad."
            );
        }
        if (medicamento.getCodigoProducto() == null || medicamento.getCodigoProducto().isBlank()
                || medicamento.getNombreComercial() == null || medicamento.getNombreComercial().isBlank()
                || medicamento.getPresentacion() == null || medicamento.getPresentacion().isBlank()) {
            throw new DatosInventarioInconsistentesException(
                    "El medicamento presenta datos comerciales incompletos en el catálogo."
            );
        }
        if (medicamento.getPrecioVenta() == null
                || medicamento.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DatosInventarioInconsistentesException(
                    "El medicamento " + medicamento.getCodigoProducto() + " registra un precio unitario inválido."
            );
        }
        if (stockActual == null) {
            throw new DatosInventarioInconsistentesException(
                    "No se pudo determinar el stock actualizado para el medicamento " + medicamento.getCodigoProducto() + "."
            );
        }
        if (stockActual < 0) {
            throw new DatosInventarioInconsistentesException(
                    "El inventario presenta un stock negativo inconsistente para el medicamento "
                            + medicamento.getCodigoProducto() + "."
            );
        }

        EstadoDisponibilidad estado = stockActual > 0
                ? EstadoDisponibilidad.DISPONIBLE
                : EstadoDisponibilidad.AGOTADO;

        return new DisponibilidadMedicamento(
                medicamento.getIdProducto(),
                medicamento.getCodigoProducto(),
                medicamento.getNombreComercial(),
                medicamento.getPrincipioActivo(),
                medicamento.getPresentacion(),
                medicamento.getLaboratorio(),
                medicamento.getPrecioVenta(),
                stockActual,
                medicamento.isRequiereReceta(),
                estado
        );
    }

    public boolean disponible() {
        return estadoDisponibilidad == EstadoDisponibilidad.DISPONIBLE;
    }
}
