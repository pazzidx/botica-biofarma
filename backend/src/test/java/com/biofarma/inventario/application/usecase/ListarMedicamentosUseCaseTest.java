package com.biofarma.inventario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.biofarma.inventario.application.dto.MedicamentoResponse;
import com.biofarma.inventario.application.mapper.MedicamentoMapper;
import com.biofarma.inventario.domain.model.Medicamento;
import com.biofarma.inventario.domain.repository.MedicamentoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListarMedicamentosUseCaseTest {

    @Mock
    private MedicamentoRepository medicamentoRepository;

    private ListarMedicamentosUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListarMedicamentosUseCase(medicamentoRepository, new MedicamentoMapper());
    }

    @Test
    void deberiaListarTodosLosMedicamentosCuandoSoloActivosEsNullOFalse() {
        Medicamento activo = Medicamento.reconstruir(
                1, "MED-001", "Paracetamol", "Paracetamol", "Tabletas",
                "Genfar", new BigDecimal("5.00"), 10, false, true, LocalDateTime.now()
        );
        Medicamento inactivo = Medicamento.reconstruir(
                2, "MED-002", "Antiguo", "Compuesto", "Jarabe",
                null, new BigDecimal("9.00"), 5, false, false, LocalDateTime.now()
        );

        when(medicamentoRepository.findAll()).thenReturn(List.of(activo, inactivo));

        List<MedicamentoResponse> resultado = useCase.ejecutar(null);

        assertEquals(2, resultado.size());
        verify(medicamentoRepository).findAll();
    }

    @Test
    void deberiaFiltrarSoloActivosCuandoSeSolicita() {
        Medicamento activo = Medicamento.reconstruir(
                1, "MED-001", "Paracetamol", "Paracetamol", "Tabletas",
                "Genfar", new BigDecimal("5.00"), 10, false, true, LocalDateTime.now()
        );

        when(medicamentoRepository.findByActivo(true)).thenReturn(List.of(activo));

        List<MedicamentoResponse> resultado = useCase.ejecutar(true);

        assertEquals(1, resultado.size());
        assertEquals("MED-001", resultado.get(0).codigoProducto());
        verify(medicamentoRepository).findByActivo(true);
    }
}
