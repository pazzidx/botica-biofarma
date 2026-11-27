package com.biofarma.inventario.domain.exception;

/**
 * Excepción de negocio lanzada cuando una trabajadora intenta consultar una venta que pertenece a otro usuario.
 */
public class AccesoVentaDenegadoException extends RuntimeException {

    public AccesoVentaDenegadoException(String message) {
        super(message);
    }
}
