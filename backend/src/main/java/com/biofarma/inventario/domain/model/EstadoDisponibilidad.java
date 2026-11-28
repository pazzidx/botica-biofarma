package com.biofarma.inventario.domain.model;

/**
 * Estado operativo de disponibilidad de un medicamento activo en mostrador (HU-05).
 */
public enum EstadoDisponibilidad {
    DISPONIBLE("Disponible"),
    AGOTADO("Agotado");

    private final String etiqueta;

    EstadoDisponibilidad(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String etiqueta() {
        return etiqueta;
    }
}
