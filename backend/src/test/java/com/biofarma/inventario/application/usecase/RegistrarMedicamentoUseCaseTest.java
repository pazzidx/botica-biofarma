package com.biofarma.inventario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.biofarma.inventario.application.dto.MedicamentoResponse;
import com.biofarma.inventario.application.dto.RegistrarMedicamentoRequest;
import com.biofarma.inventario.application.mapper.MedicamentoMapper;
import com.biofarma.inventario.domain.exception.CodigoMedicamentoDuplicadoException;
import com.biofarma.inventario.domain.exception.DatosMedicamentoInvalidosException;
import com.biofarma.inventario.domain.model.Medicamento;
import com.biofarma.inventario.domain.repository.MedicamentoRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrarMedicamentoUseCaseTest {

    @Mock
    private MedicamentoRepository medicamentoRepository;

    private RegistrarMedicamentoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegistrarMedicamentoUseCase(medicamentoRepository, new MedicamentoMapper());
    }

    @Test
    void deberiaRegistrarMedicamentoValido() {
        RegistrarMedicamentoRequest request = new RegistrarMedicamentoRequest(
                "MED-101",
                "Amoxicilina 500mg",
                "Amoxicilina",
                "Caja x 50 cápsulas",
                "Medrock",
                new BigDecimal("22.00"),
                15,
                true
        );

        when(medicamentoRepository.existsByCodigoProducto("MED-101")).thenReturn(false);
        when(medicamentoRepository.save(any(Medicamento.class))).thenAnswer(invocation -> {
            Medicamento m = invocation.getArgument(0);
            return Medicamento.reconstruir(
                    1,
                    m.getCodigoProducto(),
                    m.getNombreComercial(),
                    m.getPrincipioActivo(),
                    m.getPresentacion(),
                    m.getLaboratorio(),
                    m.getPrecioVenta(),
                    m.getStockMinimo(),
                    m.isRequiereReceta(),
                    m.isActivo(),
                    LocalDateTime.now()
            );
        });

        MedicamentoResponse response = useCase.ejecutar(request);

        assertEquals(1, response.idProducto());
        assertEquals("MED-101", response.codigoProducto());
        assertEquals(new BigDecimal("22.00"), response.precioVenta());
        assertTrue(response.requiereReceta());
        assertTrue(response.activo());
    }

    @Test
    void deberiaRechazarCodigoDuplicado() {
        RegistrarMedicamentoRequest request = new RegistrarMedicamentoRequest(
                "MED-101",
                "Amoxicilina 500mg",
                "Amoxicilina",
                "Caja x 50 cápsulas",
                "Medrock",
                new BigDecimal("22.00"),
                15,
                true
        );

        when(medicamentoRepository.existsByCodigoProducto("MED-101")).thenReturn(true);

        assertThrows(CodigoMedicamentoDuplicadoException.class, () -> useCase.ejecutar(request));
        verify(medicamentoRepository, never()).save(any());
    }

    @Test
    void deberiaRechazarDatosInvalidos() {
        RegistrarMedicamentoRequest request = new RegistrarMedicamentoRequest(
                "MED-102",
                "",
                "Paracetamol",
                "Tabletas",
                null,
                new BigDecimal("-5.00"),
                10,
                false
        );

        assertThrows(DatosMedicamentoInvalidosException.class, () -> useCase.ejecutar(request));
        verify(medicamentoRepository, never()).save(any());
    }
}
