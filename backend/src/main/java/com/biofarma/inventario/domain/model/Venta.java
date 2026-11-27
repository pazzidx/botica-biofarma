package com.biofarma.inventario.domain.model;

import com.biofarma.inventario.domain.exception.AccesoVentaDenegadoException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Entidad pura de dominio que encapsula una venta y sus reglas de control de acceso por propiedad y rol.
 */
public class Venta {

    private final Integer idVenta;
    private final String numeroTicket;
    private final Integer idUsuario;
    private final String codigoUsuario;
    private final String nombreUsuario;
    private final LocalDateTime fechaVenta;
    private final BigDecimal total;
    private final String estado;
    private final List<DetalleVenta> detalles;

    public Venta(
            Integer idVenta,
            String numeroTicket,
            Integer idUsuario,
            String codigoUsuario,
            String nombreUsuario,
            LocalDateTime fechaVenta,
            BigDecimal total,
            String estado,
            List<DetalleVenta> detalles
    ) {
        this.idVenta = idVenta;
        this.numeroTicket = numeroTicket;
        this.idUsuario = idUsuario;
        this.codigoUsuario = codigoUsuario;
        this.nombreUsuario = nombreUsuario;
        this.fechaVenta = fechaVenta;
        this.total = total;
        this.estado = estado;
        this.detalles = detalles == null ? List.of() : List.copyOf(detalles);
    }

    /**
     * Verifica si la venta fue registrada por el usuario indicado.
     */
    public boolean perteneceA(Integer idUsuarioSolicitante) {
        return idUsuarioSolicitante != null && Objects.equals(this.idUsuario, idUsuarioSolicitante);
    }

    /**
     * Valida que el usuario autenticado tenga permiso para consultar el detalle de esta venta.
     * La administradora accede a cualquier venta; una trabajadora solo a las suyas.
     */
    public void validarAcceso(AuthenticatedUser solicitante) {
        if (solicitante == null) {
            throw new AccesoVentaDenegadoException("No existe una sesión autenticada válida.");
        }
        if (solicitante.esAdministradora()) {
            return;
        }
        if (!perteneceA(solicitante.idUsuario())) {
            throw new AccesoVentaDenegadoException(
                    "No tiene permisos para consultar el detalle de una venta registrada por otra trabajadora."
            );
        }
    }

    public Integer getIdVenta() {
        return idVenta;
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public String getCodigoUsuario() {
        return codigoUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getEstado() {
        return estado;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public int getCantidadItems() {
        return detalles.stream().mapToInt(DetalleVenta::getCantidad).sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Venta venta)) return false;
        return Objects.equals(idVenta, venta.idVenta)
                && Objects.equals(numeroTicket, venta.numeroTicket);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idVenta, numeroTicket);
    }
}
