package com.biofarma.inventario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.biofarma.inventario.application.dto.MedicamentoResponse;
import com.biofarma.inventario.application.mapper.MedicamentoMapper;
import com.biofarma.inventario.domain.exception.MedicamentoNoEncontradoException;
import com.biofarma.inventario.domain.model.Medicamento;
import com.biofarma.inventario.domain.repository.MedicamentoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DesactivarMedicamentoUseCaseTest {

    @Mock
    private MedicamentoRepository medicamentoRepository;

    private DesactivarMedicamentoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DesactivarMedicamentoUseCase(medicamentoRepository, new MedicamentoMapper());
    }

    @Test
    void deberiaDesactivarLogicamenteSinEliminarRegistro() {
        Medicamento activo = Medicamento.reconstruir(
                7,
                "MED-300",
                "Omeprazol 20mg",
                "Omeprazol",
                "Caja x 30 cápsulas",
                "Genfar",
                new BigDecimal("11.00"),
                12,
                false,
                true,
                LocalDateTime.now().minusDays(10)
        );

        when(medicamentoRepository.findById(7)).thenReturn(Optional.of(activo));
        when(medicamentoRepository.save(any(Medicamento.class))).thenAnswer(inv -> inv.getArgument(0));

        MedicamentoResponse response = useCase.ejecutar(7);

        assertEquals(7, response.idProducto());
        assertFalse(response.activo());
        verify(medicamentoRepository).save(any(Medicamento.class));
    }

    @Test
    void deberiaFallarSiNoExisteAlDesactivar() {
        when(medicamentoRepository.findById(404)).thenReturn(Optional.empty());

        assertThrows(MedicamentoNoEncontradoException.class, () -> useCase.ejecutar(404));
        verify(medicamentoRepository, never()).save(any());
    }
}
