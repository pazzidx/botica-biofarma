package com.biofarma.inventario.domain.exception;

/**
 * Excepción lanzada cuando no existe un usuario con el identificador solicitado.
 */
public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(Integer idUsuario) {
        super("No se encontró un usuario con el identificador " + idUsuario + ".");
    }
}
