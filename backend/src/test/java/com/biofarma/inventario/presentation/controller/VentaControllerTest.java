package com.biofarma.inventario.presentation.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.biofarma.inventario.application.dto.DetalleVentaResponse;
import com.biofarma.inventario.application.dto.VentaDetalleResponse;
import com.biofarma.inventario.application.dto.VentaResumenResponse;
import com.biofarma.inventario.application.usecase.ListarHistorialVentasUseCase;
import com.biofarma.inventario.application.usecase.ObtenerDetalleVentaUseCase;
import com.biofarma.inventario.domain.exception.AccesoVentaDenegadoException;
import com.biofarma.inventario.infrastructure.security.SecurityConfig;
import com.biofarma.inventario.presentation.advice.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = VentaController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListarHistorialVentasUseCase listarHistorialVentasUseCase;

    @MockitoBean
    private ObtenerDetalleVentaUseCase obtenerDetalleVentaUseCase;

    @Test
    void deberiaRetornar401CuandoNoEstaAutenticado() throws Exception {
        mockMvc.perform(get("/api/v1/ventas/historial"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "trab01", roles = "TRABAJADORA")
    void deberiaListarHistorialParaTrabajadoraAutenticada() throws Exception {
        when(listarHistorialVentasUseCase.ejecutar()).thenReturn(List.of(
                new VentaResumenResponse(
                        10,
                        "T-2026-0001",
                        2,
                        "trab01",
                        "Lucia Gomez",
                        LocalDateTime.of(2026, 11, 26, 10, 0),
                        new BigDecimal("25.00"),
                        "COMPLETADA",
                        2
                )
        ));

        mockMvc.perform(get("/api/v1/ventas/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numeroTicket").value("T-2026-0001"))
                .andExpect(jsonPath("$[0].codigoUsuario").value("trab01"));
    }

    @Test
    @WithMockUser(username = "trab01", roles = "TRABAJADORA")
    void deberiaRetornar403CuandoTrabajadoraConsultaVentaDeOtraTrabajadora() throws Exception {
        when(obtenerDetalleVentaUseCase.ejecutar(11))
                .thenThrow(new AccesoVentaDenegadoException(
                        "No tiene permisos para consultar el detalle de una venta registrada por otra trabajadora."
                ));

        mockMvc.perform(get("/api/v1/ventas/11"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMINISTRADORA")
    void deberiaRetornarDetalleDeVentaParaAdministradora() throws Exception {
        when(obtenerDetalleVentaUseCase.ejecutar(11)).thenReturn(
                new VentaDetalleResponse(
                        11,
                        "T-2026-0003",
                        3,
                        "trab02",
                        "Rosa Paredes",
                        LocalDateTime.of(2026, 11, 26, 11, 0),
                        new BigDecimal("32.00"),
                        "COMPLETADA",
                        List.of(new DetalleVentaResponse(
                                1,
                                2,
                                "Amoxicilina 500mg",
                                "LOT-2026-B",
                                2,
                                new BigDecimal("16.00"),
                                new BigDecimal("32.00")
                        ))
                )
        );

        mockMvc.perform(get("/api/v1/ventas/11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroTicket").value("T-2026-0003"))
                .andExpect(jsonPath("$.detalles[0].nombreProducto").value("Amoxicilina 500mg"));
    }
}
