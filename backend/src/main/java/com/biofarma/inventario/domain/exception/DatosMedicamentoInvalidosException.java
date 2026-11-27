package com.biofarma.inventario.domain.exception;

/**
 * Excepción de negocio lanzada cuando los datos de un medicamento incumplen invariantes del dominio.
 */
public class DatosMedicamentoInvalidosException extends RuntimeException {

    public DatosMedicamentoInvalidosException(String message) {
        super(message);
    }
}
