package com.biofarma.inventario.domain.model;

import com.biofarma.inventario.domain.exception.DatosMedicamentoInvalidosException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Entidad pura de dominio que encapsula las reglas de negocio del catálogo de medicamentos (HU-04).
 */
public class Medicamento {

    private static final int MIN_CODIGO_LENGTH = 2;
    private static final int MAX_CODIGO_LENGTH = 30;
    private static final int MAX_NOMBRE_LENGTH = 150;
    private static final int MAX_PRESENTACION_LENGTH = 80;
    private static final int MAX_LABORATORIO_LENGTH = 100;
    private static final Pattern CODIGO_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");

    private final Integer idProducto;
    private final String codigoProducto;
    private final String nombreComercial;
    private final String principioActivo;
    private final String presentacion;
    private final String laboratorio;
    private final BigDecimal precioVenta;
    private final int stockMinimo;
    private final boolean requiereReceta;
    private final boolean activo;
    private final LocalDateTime fechaRegistro;

    private Medicamento(
            Integer idProducto,
            String codigoProducto,
            String nombreComercial,
            String principioActivo,
            String presentacion,
            String laboratorio,
            BigDecimal precioVenta,
            int stockMinimo,
            boolean requiereReceta,
            boolean activo,
            LocalDateTime fechaRegistro
    ) {
        this.idProducto = idProducto;
        this.codigoProducto = codigoProducto;
        this.nombreComercial = nombreComercial;
        this.principioActivo = principioActivo;
        this.presentacion = presentacion;
        this.laboratorio = laboratorio;
        this.precioVenta = precioVenta;
        this.stockMinimo = stockMinimo;
        this.requiereReceta = requiereReceta;
        this.activo = activo;
        this.fechaRegistro = fechaRegistro;
    }

    /**
     * Crea un nuevo medicamento validando todas sus invariantes de negocio.
     */
    public static Medicamento crearNuevo(
            String codigoProducto,
            String nombreComercial,
            String principioActivo,
            String presentacion,
            String laboratorio,
            BigDecimal precioVenta,
            int stockMinimo,
            boolean requiereReceta
    ) {
        return new Medicamento(
                null,
                validarCodigoProducto(codigoProducto),
                validarTextoObligatorio(nombreComercial, "El nombre comercial", MAX_NOMBRE_LENGTH),
                validarTextoObligatorio(principioActivo, "El principio activo", MAX_NOMBRE_LENGTH),
                validarTextoObligatorio(presentacion, "La presentación", MAX_PRESENTACION_LENGTH),
                normalizarTextoOpcional(laboratorio, "El laboratorio", MAX_LABORATORIO_LENGTH),
                validarPrecioVenta(precioVenta),
                validarStockMinimo(stockMinimo),
                requiereReceta,
                true,
                LocalDateTime.now()
        );
    }

    /**
     * Reconstruye un medicamento existente desde la capa de persistencia.
     */
    public static Medicamento reconstruir(
            Integer idProducto,
            String codigoProducto,
            String nombreComercial,
            String principioActivo,
            String presentacion,
            String laboratorio,
            BigDecimal precioVenta,
            int stockMinimo,
            boolean requiereReceta,
            boolean activo,
            LocalDateTime fechaRegistro
    ) {
        return new Medicamento(
                idProducto,
                codigoProducto,
                nombreComercial,
                principioActivo,
                presentacion,
                laboratorio,
                precioVenta,
                stockMinimo,
                requiereReceta,
                activo,
                fechaRegistro
        );
    }

    /**
     * Retorna una copia actualizada con los nuevos datos comerciales permitidos.
     */
    public Medicamento actualizar(
            String nombreComercial,
            String principioActivo,
            String presentacion,
            String laboratorio,
            BigDecimal precioVenta,
            int stockMinimo,
            boolean requiereReceta
    ) {
        return new Medicamento(
                this.idProducto,
                this.codigoProducto,
                validarTextoObligatorio(nombreComercial, "El nombre comercial", MAX_NOMBRE_LENGTH),
                validarTextoObligatorio(principioActivo, "El principio activo", MAX_NOMBRE_LENGTH),
                validarTextoObligatorio(presentacion, "La presentación", MAX_PRESENTACION_LENGTH),
                normalizarTextoOpcional(laboratorio, "El laboratorio", MAX_LABORATORIO_LENGTH),
                validarPrecioVenta(precioVenta),
                validarStockMinimo(stockMinimo),
                requiereReceta,
                this.activo,
                this.fechaRegistro
        );
    }

    /**
     * Aplica desactivación lógica conservando el registro histórico.
     */
    public Medicamento desactivar() {
        return new Medicamento(
                this.idProducto,
                this.codigoProducto,
                this.nombreComercial,
                this.principioActivo,
                this.presentacion,
                this.laboratorio,
                this.precioVenta,
                this.stockMinimo,
                this.requiereReceta,
                false,
                this.fechaRegistro
        );
    }

    private static String validarCodigoProducto(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new DatosMedicamentoInvalidosException("El código del medicamento es obligatorio.");
        }
        String normalizado = codigo.trim();
        if (normalizado.length() < MIN_CODIGO_LENGTH || normalizado.length() > MAX_CODIGO_LENGTH) {
            throw new DatosMedicamentoInvalidosException(
                    "El código del medicamento debe tener entre " + MIN_CODIGO_LENGTH + " y " + MAX_CODIGO_LENGTH + " caracteres."
            );
        }
        if (!CODIGO_PATTERN.matcher(normalizado).matches()) {
            throw new DatosMedicamentoInvalidosException(
                    "El código del medicamento solo puede contener letras, números, puntos, guiones y guion bajo."
            );
        }
        return normalizado;
    }

    private static String validarTextoObligatorio(String valor, String campo, int maxLength) {
        if (valor == null || valor.isBlank()) {
            throw new DatosMedicamentoInvalidosException(campo + " es obligatorio.");
        }
        String normalizado = valor.trim();
        if (normalizado.length() > maxLength) {
            throw new DatosMedicamentoInvalidosException(
                    campo + " no puede superar los " + maxLength + " caracteres."
            );
        }
        return normalizado;
    }

    private static String normalizarTextoOpcional(String valor, String campo, int maxLength) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        String normalizado = valor.trim();
        if (normalizado.length() > maxLength) {
            throw new DatosMedicamentoInvalidosException(
                    campo + " no puede superar los " + maxLength + " caracteres."
            );
        }
        return normalizado;
    }

    private static BigDecimal validarPrecioVenta(BigDecimal precioVenta) {
        if (precioVenta == null || precioVenta.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DatosMedicamentoInvalidosException("El precio unitario de venta debe ser mayor a cero.");
        }
        return precioVenta;
    }

    private static int validarStockMinimo(int stockMinimo) {
        if (stockMinimo < 0) {
            throw new DatosMedicamentoInvalidosException("El stock mínimo no puede ser negativo.");
        }
        return stockMinimo;
    }

    public Integer getIdProducto() {
        return idProducto;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public String getPrincipioActivo() {
        return principioActivo;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public String getLaboratorio() {
        return laboratorio;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public boolean isRequiereReceta() {
        return requiereReceta;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Medicamento that)) return false;
        return Objects.equals(idProducto, that.idProducto)
                && Objects.equals(codigoProducto, that.codigoProducto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProducto, codigoProducto);
    }
}
