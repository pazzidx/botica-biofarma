package com.biofarma.inventario.domain.exception;

/**
 * Excepción lanzada cuando se intenta registrar un código de usuario ya existente.
 */
public class CodigoUsuarioDuplicadoException extends RuntimeException {

    public CodigoUsuarioDuplicadoException(String codigoUsuario) {
        super("Ya existe un usuario registrado con el código '" + codigoUsuario + "'.");
    }
}
