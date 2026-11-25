package com.biofarma.inventario.domain.exception;

/**
 * Excepción lanzada cuando los datos suministrados para un usuario incumplen reglas de dominio.
 */
public class DatosUsuarioInvalidosException extends RuntimeException {

    public DatosUsuarioInvalidosException(String message) {
        super(message);
    }
}
