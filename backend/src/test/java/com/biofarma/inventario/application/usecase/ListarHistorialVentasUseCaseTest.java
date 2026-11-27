package com.biofarma.inventario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.biofarma.inventario.application.dto.VentaResumenResponse;
import com.biofarma.inventario.application.mapper.VentaMapper;
import com.biofarma.inventario.domain.model.AuthenticatedUser;
import com.biofarma.inventario.domain.model.DetalleVenta;
import com.biofarma.inventario.domain.model.RolNombre;
import com.biofarma.inventario.domain.model.Venta;
import com.biofarma.inventario.domain.repository.VentaRepository;
import com.biofarma.inventario.domain.service.AuthenticatedUserProvider;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListarHistorialVentasUseCaseTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private ListarHistorialVentasUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListarHistorialVentasUseCase(ventaRepository, authenticatedUserProvider, new VentaMapper());
    }

    @Test
    void deberiaFiltrarPorUsuariaAutenticadaCuandoRolEsTrabajadora() {
        when(authenticatedUserProvider.obtenerUsuarioAutenticado())
                .thenReturn(new AuthenticatedUser(2, "trab01", RolNombre.TRABAJADORA));

        Venta ventaPropia = crearVenta(10, "T-2026-0001", 2, "trab01", "Lucia Gomez");
        when(ventaRepository.listarPorUsuarioOrdenadasPorFechaDesc(2)).thenReturn(List.of(ventaPropia));

        List<VentaResumenResponse> resultado = useCase.ejecutar();

        assertEquals(1, resultado.size());
        assertEquals("T-2026-0001", resultado.get(0).numeroTicket());
        assertEquals(2, resultado.get(0).idUsuario());
        verify(ventaRepository).listarPorUsuarioOrdenadasPorFechaDesc(2);
        verify(ventaRepository, never()).listarTodasOrdenadasPorFechaDesc();
    }

    @Test
    void deberiaListarTodoElHistorialCuandoRolEsAdministradora() {
        when(authenticatedUserProvider.obtenerUsuarioAutenticado())
                .thenReturn(new AuthenticatedUser(1, "admin", RolNombre.ADMINISTRADORA));

        Venta ventaTrab1 = crearVenta(10, "T-2026-0001", 2, "trab01", "Lucia Gomez");
        Venta ventaTrab2 = crearVenta(11, "T-2026-0003", 3, "trab02", "Rosa Paredes");
        when(ventaRepository.listarTodasOrdenadasPorFechaDesc()).thenReturn(List.of(ventaTrab2, ventaTrab1));

        List<VentaResumenResponse> resultado = useCase.ejecutar();

        assertEquals(2, resultado.size());
        verify(ventaRepository).listarTodasOrdenadasPorFechaDesc();
        verify(ventaRepository, never()).listarPorUsuarioOrdenadasPorFechaDesc(1);
    }

    private Venta crearVenta(Integer idVenta, String ticket, Integer idUsuario, String codigo, String nombre) {
        DetalleVenta detalle = new DetalleVenta(1, 1, "Paracetamol 500mg", "LOT-2026-A", 2, new BigDecimal("12.50"), new BigDecimal("25.00"));
        return new Venta(
                idVenta,
                ticket,
                idUsuario,
                codigo,
                nombre,
                LocalDateTime.of(2026, 11, 26, 10, 0),
                new BigDecimal("25.00"),
                "COMPLETADA",
                List.of(detalle)
        );
    }
}
