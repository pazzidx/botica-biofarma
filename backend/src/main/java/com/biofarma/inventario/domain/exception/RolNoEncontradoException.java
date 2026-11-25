package com.biofarma.inventario.domain.exception;

/**
 * Excepción lanzada cuando un rol solicitado no existe en el catálogo del sistema.
 */
public class RolNoEncontradoException extends RuntimeException {

    public RolNoEncontradoException(String nombreRol) {
        super("No se encontró el rol '" + nombreRol + "' en la base de datos.");
    }
}
