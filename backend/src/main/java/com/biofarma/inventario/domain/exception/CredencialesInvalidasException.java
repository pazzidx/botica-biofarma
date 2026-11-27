package com.biofarma.inventario.domain.exception;

/**
 * Excepción de dominio lanzada cuando las credenciales de inicio de sesión son inválidas o el usuario está inactivo.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos.");
    }
}
