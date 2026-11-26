package com.biofarma.inventario.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.biofarma.inventario.domain.exception.DatosMedicamentoInvalidosException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MedicamentoTest {

    @Test
    void deberiaCrearMedicamentoValidoNormalizandoCampos() {
        Medicamento medicamento = Medicamento.crearNuevo(
                " MED-001 ",
                " Paracetamol 500mg ",
                " Paracetamol ",
                " Caja x 100 tabletas ",
                " Genfar ",
                new BigDecimal("12.50"),
                10,
                false
        );

        assertNull(medicamento.getIdProducto());
        assertEquals("MED-001", medicamento.getCodigoProducto());
        assertEquals("Paracetamol 500mg", medicamento.getNombreComercial());
        assertEquals("Paracetamol", medicamento.getPrincipioActivo());
        assertEquals("Caja x 100 tabletas", medicamento.getPresentacion());
        assertEquals("Genfar", medicamento.getLaboratorio());
        assertEquals(new BigDecimal("12.50"), medicamento.getPrecioVenta());
        assertEquals(10, medicamento.getStockMinimo());
        assertFalse(medicamento.isRequiereReceta());
        assertTrue(medicamento.isActivo());
    }

    @Test
    void deberiaRechazarPrecioNegativoOCeroYStockMinimoNegativo() {
        assertThrows(DatosMedicamentoInvalidosException.class, () -> Medicamento.crearNuevo(
                "MED-002",
                "Amoxicilina 500mg",
                "Amoxicilina",
                "Caja x 50 cápsulas",
                null,
                BigDecimal.ZERO,
                5,
                true
        ));

        assertThrows(DatosMedicamentoInvalidosException.class, () -> Medicamento.crearNuevo(
                "MED-002",
                "Amoxicilina 500mg",
                "Amoxicilina",
                "Caja x 50 cápsulas",
                null,
                new BigDecimal("15.00"),
                -1,
                true
        ));
    }

    @Test
    void deberiaRechazarNombreOCodigoVacio() {
        assertThrows(DatosMedicamentoInvalidosException.class, () -> Medicamento.crearNuevo(
                "   ",
                "Ibuprofeno",
                "Ibuprofeno",
                "Tabletas",
                null,
                new BigDecimal("8.00"),
                5,
                false
        ));

        assertThrows(DatosMedicamentoInvalidosException.class, () -> Medicamento.crearNuevo(
                "MED-003",
                "",
                "Ibuprofeno",
                "Tabletas",
                null,
                new BigDecimal("8.00"),
                5,
                false
        ));
    }

    @Test
    void deberiaActualizarDatosYDesactivarLogicamente() {
        Medicamento original = Medicamento.crearNuevo(
                "MED-004",
                "Azitromicina 500mg",
                "Azitromicina",
                "Caja x 3 tabletas",
                "Portugal",
                new BigDecimal("18.00"),
                5,
                true
        );

        Medicamento actualizado = original.actualizar(
                "Azitromicina 500mg Forte",
                "Azitromicina Dihidrato",
                "Caja x 6 tabletas",
                "Farmindustria",
                new BigDecimal("24.50"),
                8,
                false
        );

        assertEquals("MED-004", actualizado.getCodigoProducto());
        assertEquals("Azitromicina 500mg Forte", actualizado.getNombreComercial());
        assertEquals(new BigDecimal("24.50"), actualizado.getPrecioVenta());
        assertEquals(8, actualizado.getStockMinimo());
        assertFalse(actualizado.isRequiereReceta());
        assertTrue(actualizado.isActivo());

        Medicamento desactivado = actualizado.desactivar();
        assertFalse(desactivado.isActivo());
        assertEquals("MED-004", desactivado.getCodigoProducto());
    }
}
