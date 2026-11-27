package com.biofarma.inventario.domain.exception;

/**
 * Excepción de negocio lanzada cuando no se encuentra un medicamento por su identificador.
 */
public class MedicamentoNoEncontradoException extends RuntimeException {

    public MedicamentoNoEncontradoException(Integer idProducto) {
        super("No se encontró el medicamento con ID " + idProducto + ".");
    }
}
