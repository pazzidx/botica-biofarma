package com.biofarma.inventario.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad pura de dominio que representa una línea de detalle dentro de una venta.
 */
public class DetalleVenta {

    private final Integer idDetalle;
    private final Integer idLote;
    private final String nombreProducto;
    private final String numeroLote;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;

    public DetalleVenta(
            Integer idDetalle,
            Integer idLote,
            String nombreProducto,
            String numeroLote,
            int cantidad,
            BigDecimal precioUnitario,
            BigDecimal subtotal
    ) {
        this.idDetalle = idDetalle;
        this.idLote = idLote;
        this.nombreProducto = nombreProducto;
        this.numeroLote = numeroLote;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Integer getIdDetalle() {
        return idDetalle;
    }

    public Integer getIdLote() {
        return idLote;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetalleVenta that)) return false;
        return Objects.equals(idDetalle, that.idDetalle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDetalle);
    }
}
