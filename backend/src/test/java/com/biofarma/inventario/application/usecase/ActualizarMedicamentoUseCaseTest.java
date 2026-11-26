package com.biofarma.inventario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.biofarma.inventario.application.dto.ActualizarMedicamentoRequest;
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
class ActualizarMedicamentoUseCaseTest {

    @Mock
    private MedicamentoRepository medicamentoRepository;

    private ActualizarMedicamentoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ActualizarMedicamentoUseCase(medicamentoRepository, new MedicamentoMapper());
    }

    @Test
    void deberiaActualizarDatosComercialesDeMedicamentoExistente() {
        Medicamento existente = Medicamento.reconstruir(
                5,
                "MED-200",
                "Losartán 50mg",
                "Losartán Potásico",
                "Caja x 30 tabletas",
                "Genfar",
                new BigDecimal("14.00"),
                10,
                true,
                true,
                LocalDateTime.now().minusDays(2)
        );
        ActualizarMedicamentoRequest request = new ActualizarMedicamentoRequest(
                "Losartán 50mg Forte",
                "Losartán Potásico",
                "Caja x 60 tabletas",
                "Portugal",
                new BigDecimal("26.50"),
                20,
                false
        );

        when(medicamentoRepository.findById(5)).thenReturn(Optional.of(existente));
        when(medicamentoRepository.save(any(Medicamento.class))).thenAnswer(inv -> inv.getArgument(0));

        MedicamentoResponse response = useCase.ejecutar(5, request);

        assertEquals(5, response.idProducto());
        assertEquals("MED-200", response.codigoProducto());
        assertEquals("Losartán 50mg Forte", response.nombreComercial());
        assertEquals(new BigDecimal("26.50"), response.precioVenta());
        assertEquals(20, response.stockMinimo());
        assertFalse(response.requiereReceta());
    }

    @Test
    void deberiaLanzarExcepcionSiMedicamentoNoExiste() {
        ActualizarMedicamentoRequest request = new ActualizarMedicamentoRequest(
                "Losartán 50mg",
                "Losartán",
                "Caja x 30",
                null,
                new BigDecimal("14.00"),
                10,
                false
        );

        when(medicamentoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(MedicamentoNoEncontradoException.class, () -> useCase.ejecutar(99, request));
        verify(medicamentoRepository, never()).save(any());
    }
}
