package com.biofarma.inventario.domain.exception;

/**
 * Excepción de dominio lanzada cuando los datos de stock o precio en el inventario
 * presentan inconsistencias (ej. stock negativo, stock nulo o precio inválido).
 */
public class DatosInventarioInconsistentesException extends RuntimeException {

    public DatosInventarioInconsistentesException(String message) {
        super(message);
    }
}
