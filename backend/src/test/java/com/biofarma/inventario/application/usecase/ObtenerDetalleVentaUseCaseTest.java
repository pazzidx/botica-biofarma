package com.biofarma.inventario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.biofarma.inventario.application.dto.VentaDetalleResponse;
import com.biofarma.inventario.application.mapper.VentaMapper;
import com.biofarma.inventario.domain.exception.AccesoVentaDenegadoException;
import com.biofarma.inventario.domain.exception.VentaNoEncontradaException;
import com.biofarma.inventario.domain.model.AuthenticatedUser;
import com.biofarma.inventario.domain.model.DetalleVenta;
import com.biofarma.inventario.domain.model.RolNombre;
import com.biofarma.inventario.domain.model.Venta;
import com.biofarma.inventario.domain.repository.VentaRepository;
import com.biofarma.inventario.domain.service.AuthenticatedUserProvider;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ObtenerDetalleVentaUseCaseTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    private ObtenerDetalleVentaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ObtenerDetalleVentaUseCase(ventaRepository, authenticatedUserProvider, new VentaMapper());
    }

    @Test
    void deberiaPermitirVerDetalleDeVentaPropiaATrabajadora() {
        when(authenticatedUserProvider.obtenerUsuarioAutenticado())
                .thenReturn(new AuthenticatedUser(2, "trab01", RolNombre.TRABAJADORA));

        Venta venta = crearVenta(10, "T-2026-0001", 2, "trab01");
        when(ventaRepository.buscarPorIdConDetalle(10)).thenReturn(Optional.of(venta));

        VentaDetalleResponse response = useCase.ejecutar(10);

        assertEquals(10, response.idVenta());
        assertEquals("T-2026-0001", response.numeroTicket());
        assertEquals(1, response.detalles().size());
    }

    @Test
    void deberiaRechazarAccesoCuandoTrabajadoraConsultaVentaAjena() {
        when(authenticatedUserProvider.obtenerUsuarioAutenticado())
                .thenReturn(new AuthenticatedUser(2, "trab01", RolNombre.TRABAJADORA));

        Venta ventaAjena = crearVenta(11, "T-2026-0003", 3, "trab02");
        when(ventaRepository.buscarPorIdConDetalle(11)).thenReturn(Optional.of(ventaAjena));

        assertThrows(AccesoVentaDenegadoException.class, () -> useCase.ejecutar(11));
    }

    @Test
    void deberiaPermitirVerCualquierVentaAAdministradora() {
        when(authenticatedUserProvider.obtenerUsuarioAutenticado())
                .thenReturn(new AuthenticatedUser(1, "admin", RolNombre.ADMINISTRADORA));

        Venta ventaDeTrabajadora = crearVenta(11, "T-2026-0003", 3, "trab02");
        when(ventaRepository.buscarPorIdConDetalle(11)).thenReturn(Optional.of(ventaDeTrabajadora));

        VentaDetalleResponse response = useCase.ejecutar(11);

        assertEquals(11, response.idVenta());
        assertEquals("trab02", response.codigoUsuario());
    }

    @Test
    void deberiaLanzarExcepcionCuandoVentaNoExiste() {
        when(authenticatedUserProvider.obtenerUsuarioAutenticado())
                .thenReturn(new AuthenticatedUser(1, "admin", RolNombre.ADMINISTRADORA));
        when(ventaRepository.buscarPorIdConDetalle(999)).thenReturn(Optional.empty());

        assertThrows(VentaNoEncontradaException.class, () -> useCase.ejecutar(999));
    }

    private Venta crearVenta(Integer idVenta, String ticket, Integer idUsuario, String codigo) {
        DetalleVenta detalle = new DetalleVenta(1, 1, "Paracetamol 500mg", "LOT-2026-A", 2, new BigDecimal("12.50"), new BigDecimal("25.00"));
        return new Venta(
                idVenta,
                ticket,
                idUsuario,
                codigo,
                "Lucia Gomez",
                LocalDateTime.of(2026, 11, 26, 10, 0),
                new BigDecimal("25.00"),
                "COMPLETADA",
                List.of(detalle)
        );
    }
}
