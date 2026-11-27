package com.biofarma.inventario.domain.exception;

/**
 * Excepción lanzada cuando se intenta modificar o eliminar una cuenta protegida o la sesión actual.
 */
public class OperacionNoPermitidaException extends RuntimeException {

    public OperacionNoPermitidaException(String message) {
        super(message);
    }
}
