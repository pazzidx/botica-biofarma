package com.biofarma.inventario.presentation.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.biofarma.inventario.application.dto.ActualizarMedicamentoRequest;
import com.biofarma.inventario.application.dto.MedicamentoResponse;
import com.biofarma.inventario.application.dto.RegistrarMedicamentoRequest;
import com.biofarma.inventario.application.usecase.ActualizarMedicamentoUseCase;
import com.biofarma.inventario.application.usecase.DesactivarMedicamentoUseCase;
import com.biofarma.inventario.application.usecase.ListarMedicamentosUseCase;
import com.biofarma.inventario.application.usecase.RegistrarMedicamentoUseCase;
import com.biofarma.inventario.domain.exception.CodigoMedicamentoDuplicadoException;
import com.biofarma.inventario.infrastructure.security.SecurityConfig;
import com.biofarma.inventario.presentation.advice.GlobalExceptionHandler;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = MedicamentoController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class MedicamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegistrarMedicamentoUseCase registrarMedicamentoUseCase;

    @MockitoBean
    private ActualizarMedicamentoUseCase actualizarMedicamentoUseCase;

    @MockitoBean
    private DesactivarMedicamentoUseCase desactivarMedicamentoUseCase;

    @MockitoBean
    private ListarMedicamentosUseCase listarMedicamentosUseCase;

    @Test
    void deberiaRetornarUnauthorizedSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/medicamentos"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/medicamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "TRABAJADORA")
    void deberiaDenegarAccesoATrabajadoraEnOperacionesDeCatalogo() throws Exception {
        RegistrarMedicamentoRequest request = new RegistrarMedicamentoRequest(
                "MED-001", "Paracetamol", "Paracetamol", "Tabletas",
                "Genfar", new BigDecimal("5.00"), 10, false
        );

        mockMvc.perform(post("/api/v1/medicamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        ActualizarMedicamentoRequest updateRequest = new ActualizarMedicamentoRequest(
                "Paracetamol", "Paracetamol", "Tabletas",
                "Genfar", new BigDecimal("6.00"), 10, false
        );

        mockMvc.perform(put("/api/v1/medicamentos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/v1/medicamentos/1/desactivar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADORA")
    void deberiaRegistrarMedicamentoCuandoEsAdministradora() throws Exception {
        RegistrarMedicamentoRequest request = new RegistrarMedicamentoRequest(
                "MED-001", "Paracetamol 500mg", "Paracetamol", "Caja x 100",
                "Genfar", new BigDecimal("12.50"), 10, false
        );
        MedicamentoResponse response = new MedicamentoResponse(
                1, "MED-001", "Paracetamol 500mg", "Paracetamol", "Caja x 100",
                "Genfar", new BigDecimal("12.50"), 10, false, true, LocalDateTime.now()
        );

        when(registrarMedicamentoUseCase.ejecutar(any(RegistrarMedicamentoRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/medicamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto").value(1))
                .andExpect(jsonPath("$.codigoProducto").value("MED-001"))
                .andExpect(jsonPath("$.precioVenta").value(12.50))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADORA")
    void deberiaRetornarBadRequestCuandoFaltanCamposOValoresSonInvalidos() throws Exception {
        RegistrarMedicamentoRequest requestInvalido = new RegistrarMedicamentoRequest(
                "", "", "", "", null, new BigDecimal("-1.00"), -5, null
        );

        mockMvc.perform(post("/api/v1/medicamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.codigoProducto").exists())
                .andExpect(jsonPath("$.fieldErrors.nombreComercial").exists())
                .andExpect(jsonPath("$.fieldErrors.precioVenta").exists())
                .andExpect(jsonPath("$.fieldErrors.stockMinimo").exists());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADORA")
    void deberiaRetornarConflictCuandoCodigoYaExiste() throws Exception {
        RegistrarMedicamentoRequest request = new RegistrarMedicamentoRequest(
                "MED-001", "Paracetamol 500mg", "Paracetamol", "Caja x 100",
                "Genfar", new BigDecimal("12.50"), 10, false
        );

        when(registrarMedicamentoUseCase.ejecutar(any(RegistrarMedicamentoRequest.class)))
                .thenThrow(new CodigoMedicamentoDuplicadoException("MED-001"));

        mockMvc.perform(post("/api/v1/medicamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("MED-001")));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADORA")
    void deberiaActualizarYDesactivarMedicamento() throws Exception {
        ActualizarMedicamentoRequest updateRequest = new ActualizarMedicamentoRequest(
                "Paracetamol Forte", "Paracetamol", "Caja x 50",
                "Medrock", new BigDecimal("15.00"), 8, true
        );
        MedicamentoResponse actualizado = new MedicamentoResponse(
                1, "MED-001", "Paracetamol Forte", "Paracetamol", "Caja x 50",
                "Medrock", new BigDecimal("15.00"), 8, true, true, LocalDateTime.now()
        );
        MedicamentoResponse desactivado = new MedicamentoResponse(
                1, "MED-001", "Paracetamol Forte", "Paracetamol", "Caja x 50",
                "Medrock", new BigDecimal("15.00"), 8, true, false, LocalDateTime.now()
        );

        when(actualizarMedicamentoUseCase.ejecutar(eq(1), any(ActualizarMedicamentoRequest.class)))
                .thenReturn(actualizado);
        when(desactivarMedicamentoUseCase.ejecutar(1)).thenReturn(desactivado);

        mockMvc.perform(put("/api/v1/medicamentos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreComercial").value("Paracetamol Forte"))
                .andExpect(jsonPath("$.requiereReceta").value(true));

        mockMvc.perform(patch("/api/v1/medicamentos/1/desactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADORA")
    void deberiaListarMedicamentos() throws Exception {
        when(listarMedicamentosUseCase.ejecutar(null)).thenReturn(List.of(
                new MedicamentoResponse(
                        1, "MED-001", "Paracetamol", "Paracetamol", "Tabletas",
                        "Genfar", new BigDecimal("5.00"), 10, false, true, LocalDateTime.now()
                )
        ));

        mockMvc.perform(get("/api/v1/medicamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoProducto").value("MED-001"));
    }

    @Test
    @WithMockUser(roles = "TRABAJADORA")
    void deberiaForzarSoloActivosParaTrabajadoraAunqueSoliciteSoloActivosFalse() throws Exception {
        when(listarMedicamentosUseCase.ejecutar(Boolean.TRUE)).thenReturn(List.of(
                new MedicamentoResponse(
                        1, "MED-001", "Paracetamol", "Paracetamol", "Tabletas",
                        "Genfar", new BigDecimal("5.00"), 10, false, true, LocalDateTime.now()
                )
        ));

        mockMvc.perform(get("/api/v1/medicamentos").param("soloActivos", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoProducto").value("MED-001"))
                .andExpect(jsonPath("$[0].activo").value(true));

        verify(listarMedicamentosUseCase).ejecutar(Boolean.TRUE);
        verify(listarMedicamentosUseCase, never()).ejecutar(Boolean.FALSE);
    }
}
