package com.biofarma.inventario.domain.exception;

/**
 * Excepción de negocio lanzada cuando ya existe un medicamento con el mismo código.
 */
public class CodigoMedicamentoDuplicadoException extends RuntimeException {

    public CodigoMedicamentoDuplicadoException(String codigoProducto) {
        super("Ya existe un medicamento registrado con el código '" + codigoProducto + "'.");
    }
}
