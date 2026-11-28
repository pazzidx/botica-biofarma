package com.biofarma.inventario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.biofarma.inventario.application.dto.MedicamentoDisponibilidadResponse;
import com.biofarma.inventario.application.mapper.DisponibilidadMedicamentoMapper;
import com.biofarma.inventario.domain.exception.DatosInventarioInconsistentesException;
import com.biofarma.inventario.domain.exception.DatosMedicamentoInvalidosException;
import com.biofarma.inventario.domain.model.Medicamento;
import com.biofarma.inventario.domain.repository.MedicamentoRepository;
import com.biofarma.inventario.domain.repository.StockInventarioRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultarDisponibilidadMedicamentosUseCaseTest {

    @Mock
    private MedicamentoRepository medicamentoRepository;

    @Mock
    private StockInventarioRepository stockInventarioRepository;

    private ConsultarDisponibilidadMedicamentosUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarDisponibilidadMedicamentosUseCase(
                medicamentoRepository,
                stockInventarioRepository,
                new DisponibilidadMedicamentoMapper()
        );
    }

    @Test
    void deberiaBuscarPorCodigoNumericoONormalizadoYRetornarStockDisponible() {
        Medicamento paracetamol = Medicamento.reconstruir(
                10, "7501001", "Paracetamol 500mg", "Paracetamol", "Caja x 20 tabletas",
                "Genfar", new BigDecimal("12.50"), 5, false, true, LocalDateTime.now()
        );

        when(medicamentoRepository.buscarActivosPorTermino("7501001", 50))
                .thenReturn(List.of(paracetamol));
        when(stockInventarioRepository.obtenerStockActualPorProductos(List.of(10)))
                .thenReturn(Map.of(10, 34));

        List<MedicamentoDisponibilidadResponse> resultado = useCase.ejecutar("  7501001  ", null);

        assertEquals(1, resultado.size());
        MedicamentoDisponibilidadResponse item = resultado.get(0);
        assertEquals("7501001", item.codigoProducto());
        assertEquals("Paracetamol 500mg", item.nombreComercial());
        assertEquals(new BigDecimal("12.50"), item.precioVenta());
        assertEquals(34, item.stockActual());
        assertEquals("Disponible", item.estadoDisponibilidad());
        assertTrue(item.disponible());
        verify(medicamentoRepository).buscarActivosPorTermino("7501001", 50);
    }

    @Test
    void deberiaRetornarListaConEstadosDisponibleYAgotadoEnBusquedaParcialPorNombre() {
        Medicamento conStock = Medicamento.reconstruir(
                1, "MED-001", "Amoxicilina 500mg", "Amoxicilina", "Caja x 12 cápsulas",
                "Portugal", new BigDecimal("16.00"), 10, true, true, LocalDateTime.now()
        );
        Medicamento sinStock = Medicamento.reconstruir(
                2, "MED-002", "Amoxicilina + Ácido Clavulánico", "Amoxicilina", "Frasco 60ml",
                "Medrock", new BigDecimal("28.90"), 5, true, true, LocalDateTime.now()
        );

        when(medicamentoRepository.buscarActivosPorTermino("Amoxi", 20))
                .thenReturn(List.of(conStock, sinStock));
        when(stockInventarioRepository.obtenerStockActualPorProductos(List.of(1, 2)))
                .thenReturn(Map.of(1, 15, 2, 0));

        List<MedicamentoDisponibilidadResponse> resultado = useCase.ejecutar("Amoxi", 20);

        assertEquals(2, resultado.size());
        assertEquals("Disponible", resultado.get(0).estadoDisponibilidad());
        assertTrue(resultado.get(0).disponible());
        assertEquals(15, resultado.get(0).stockActual());

        assertEquals("Agotado", resultado.get(1).estadoDisponibilidad());
        assertFalse(resultado.get(1).disponible());
        assertEquals(0, resultado.get(1).stockActual());
    }

    @Test
    void deberiaRetornarListaVaciaSinConsultarStockCuandoNoHayCoincidencias() {
        when(medicamentoRepository.buscarActivosPorTermino("InexistenteXYZ", 50))
                .thenReturn(List.of());

        List<MedicamentoDisponibilidadResponse> resultado = useCase.ejecutar("InexistenteXYZ", null);

        assertTrue(resultado.isEmpty());
        verify(stockInventarioRepository, never()).obtenerStockActualPorProductos(anyCollection());
    }

    @Test
    void deberiaRechazarTerminoVacioOConSoloEspaciosOExtenso() {
        assertThrows(DatosMedicamentoInvalidosException.class, () -> useCase.ejecutar(null, null));
        assertThrows(DatosMedicamentoInvalidosException.class, () -> useCase.ejecutar("", null));
        assertThrows(DatosMedicamentoInvalidosException.class, () -> useCase.ejecutar("     ", null));
        assertThrows(DatosMedicamentoInvalidosException.class, () -> useCase.ejecutar("a".repeat(151), null));

        verify(medicamentoRepository, never()).buscarActivosPorTermino(anyString(), anyInt());
    }

    @Test
    void deberiaLanzarExcepcionControladaSiInventarioReportaStockNegativoOFaltante() {
        Medicamento med = Medicamento.reconstruir(
                7, "MED-007", "Omeprazol 20mg", "Omeprazol", "Cápsulas",
                "Genfar", new BigDecimal("11.00"), 5, false, true, LocalDateTime.now()
        );

        when(medicamentoRepository.buscarActivosPorTermino("Omeprazol", 50))
                .thenReturn(List.of(med));
        when(stockInventarioRepository.obtenerStockActualPorProductos(List.of(7)))
                .thenReturn(Map.of(7, -1));

        assertThrows(
                DatosInventarioInconsistentesException.class,
                () -> useCase.ejecutar("Omeprazol", null)
        );
    }

    @Test
    void deberiaEvitarDuplicadosSiRepositorioRetornaRegistrosRepetidos() {
        Medicamento med = Medicamento.reconstruir(
                9, "MED-009", "Cetirizina 10mg", "Cetirizina", "Tabletas",
                "Genfar", new BigDecimal("6.50"), 5, false, true, LocalDateTime.now()
        );

        when(medicamentoRepository.buscarActivosPorTermino("Cetirizina", 50))
                .thenReturn(List.of(med, med));
        when(stockInventarioRepository.obtenerStockActualPorProductos(List.of(9)))
                .thenReturn(Map.of(9, 12));

        List<MedicamentoDisponibilidadResponse> resultado = useCase.ejecutar("Cetirizina", null);

        assertEquals(1, resultado.size());
        assertEquals(12, resultado.get(0).stockActual());
    }
}
