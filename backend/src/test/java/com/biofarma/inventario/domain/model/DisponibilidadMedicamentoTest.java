package com.biofarma.inventario.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.biofarma.inventario.domain.exception.DatosInventarioInconsistentesException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class DisponibilidadMedicamentoTest {

    @Test
    void deberiaMarcarDisponibleCuandoMedicamentoActivoTieneStockPositivo() {
        Medicamento medicamento = Medicamento.reconstruir(
                1,
                "MED-001",
                "Paracetamol 500mg",
                "Paracetamol",
                "Caja x 20 tabletas",
                "Genfar",
                new BigDecimal("12.50"),
                10,
                false,
                true,
                LocalDateTime.now()
        );

        DisponibilidadMedicamento disponibilidad = DisponibilidadMedicamento.evaluar(medicamento, 25);

        assertEquals(1, disponibilidad.idProducto());
        assertEquals("MED-001", disponibilidad.codigoProducto());
        assertEquals("Paracetamol 500mg", disponibilidad.nombreComercial());
        assertEquals("Paracetamol", disponibilidad.principioActivo());
        assertEquals("Caja x 20 tabletas", disponibilidad.presentacion());
        assertEquals("Genfar", disponibilidad.laboratorio());
        assertEquals(new BigDecimal("12.50"), disponibilidad.precioVenta());
        assertEquals(25, disponibilidad.stockActual());
        assertEquals(EstadoDisponibilidad.DISPONIBLE, disponibilidad.estadoDisponibilidad());
        assertEquals("Disponible", disponibilidad.estadoDisponibilidad().etiqueta());
        assertTrue(disponibilidad.disponible());
    }

    @Test
    void deberiaMarcarAgotadoCuandoStockEsCero() {
        Medicamento medicamento = Medicamento.reconstruir(
                2,
                "MED-002",
                "Amoxicilina 500mg",
                "Amoxicilina",
                "Caja x 12 cápsulas",
                "Portugal",
                new BigDecimal("16.00"),
                5,
                true,
                true,
                LocalDateTime.now()
        );

        DisponibilidadMedicamento disponibilidad = DisponibilidadMedicamento.evaluar(medicamento, 0);

        assertEquals(0, disponibilidad.stockActual());
        assertEquals(EstadoDisponibilidad.AGOTADO, disponibilidad.estadoDisponibilidad());
        assertEquals("Agotado", disponibilidad.estadoDisponibilidad().etiqueta());
        assertFalse(disponibilidad.disponible());
        assertTrue(disponibilidad.requiereReceta());
    }

    @Test
    void deberiaRechazarStockNegativoSinConvertirloEnCero() {
        Medicamento medicamento = Medicamento.reconstruir(
                3,
                "7501234",
                "Ibuprofeno 400mg",
                "Ibuprofeno",
                "Blíster x 10",
                "Genfar",
                new BigDecimal("4.50"),
                5,
                false,
                true,
                LocalDateTime.now()
        );

        assertThrows(
                DatosInventarioInconsistentesException.class,
                () -> DisponibilidadMedicamento.evaluar(medicamento, -3)
        );
    }

    @Test
    void deberiaRechazarStockNuloOPrecioInvalidoOMedicamentoInactivo() {
        Medicamento precioCero = Medicamento.reconstruir(
                4,
                "MED-004",
                "Loratadina 10mg",
                "Loratadina",
                "Tabletas",
                null,
                BigDecimal.ZERO,
                5,
                false,
                true,
                LocalDateTime.now()
        );
        Medicamento inactivo = Medicamento.reconstruir(
                5,
                "MED-005",
                "Antiguo",
                "Compuesto",
                "Jarabe",
                null,
                new BigDecimal("8.00"),
                5,
                false,
                false,
                LocalDateTime.now()
        );
        Medicamento activo = Medicamento.reconstruir(
                6,
                "MED-006",
                "Naproxeno 550mg",
                "Naproxeno",
                "Tabletas",
                "Medrock",
                new BigDecimal("9.50"),
                5,
                false,
                true,
                LocalDateTime.now()
        );

        assertThrows(
                DatosInventarioInconsistentesException.class,
                () -> DisponibilidadMedicamento.evaluar(precioCero, 10)
        );
        assertThrows(
                DatosInventarioInconsistentesException.class,
                () -> DisponibilidadMedicamento.evaluar(inactivo, 10)
        );
        assertThrows(
                DatosInventarioInconsistentesException.class,
                () -> DisponibilidadMedicamento.evaluar(activo, null)
        );
    }
}
