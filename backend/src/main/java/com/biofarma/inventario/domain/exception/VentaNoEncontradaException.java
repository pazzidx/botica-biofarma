package com.biofarma.inventario.domain.exception;

/**
 * Excepción de negocio lanzada cuando no existe una venta con el identificador solicitado.
 */
public class VentaNoEncontradaException extends RuntimeException {

    public VentaNoEncontradaException(Integer idVenta) {
        super("No se encontró la venta con identificador " + idVenta + ".");
    }
}
