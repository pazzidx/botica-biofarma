package com.biofarma.inventario.domain.exception;

/**
 * Excepción de negocio lanzada cuando las credenciales de acceso son incorrectas
 * o la cuenta de usuario se encuentra inactiva.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Credenciales inválidas.");
    }

    public CredencialesInvalidasException(String message) {
        super(message);
    }
}
