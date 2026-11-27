package com.biofarma.inventario.presentation.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.biofarma.inventario.application.dto.RegistrarUsuarioRequest;
import com.biofarma.inventario.application.dto.UsuarioResponse;
import com.biofarma.inventario.application.usecase.CambiarEstadoUsuarioUseCase;
import com.biofarma.inventario.application.usecase.EliminarUsuarioUseCase;
import com.biofarma.inventario.application.usecase.ListarUsuariosUseCase;
import com.biofarma.inventario.application.usecase.RegistrarUsuarioUseCase;
import com.biofarma.inventario.domain.exception.CodigoUsuarioDuplicadoException;
import com.biofarma.inventario.infrastructure.security.SecurityConfig;
import com.biofarma.inventario.presentation.advice.GlobalExceptionHandler;
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

@WebMvcTest(controllers = UsuarioController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@WithMockUser(username = "admin", roles = "ADMINISTRADORA")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @MockitoBean
    private ListarUsuariosUseCase listarUsuariosUseCase;

    @MockitoBean
    private EliminarUsuarioUseCase eliminarUsuarioUseCase;

    @MockitoBean
    private CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase;

    @Test
    void deberiaRegistrarUsuarioYNoExponerPassword() throws Exception {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "vendedor01",
                "Carla",
                "Mendoza",
                "Clave1234",
                "TRABAJADORA"
        );
        UsuarioResponse response = new UsuarioResponse(
                2,
                "vendedor01",
                "Carla",
                "Mendoza",
                "TRABAJADORA",
                true,
                LocalDateTime.of(2026, 11, 24, 21, 50)
        );

        when(registrarUsuarioUseCase.ejecutar(any(RegistrarUsuarioRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idUsuario").value(2))
                .andExpect(jsonPath("$.codigoUsuario").value("vendedor01"))
                .andExpect(jsonPath("$.rol").value("TRABAJADORA"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(content().string(not(containsString("passwordHash"))))
                .andExpect(content().string(not(containsString("Clave1234"))));
    }

    @Test
    void deberiaRetornarBadRequestCuandoFaltanCampos() throws Exception {
        RegistrarUsuarioRequest requestInvalido = new RegistrarUsuarioRequest(
                "",
                "",
                "Mendoza",
                "123",
                ""
        );

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.codigoUsuario").exists())
                .andExpect(jsonPath("$.fieldErrors.nombres").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists())
                .andExpect(jsonPath("$.fieldErrors.rol").exists());
    }

    @Test
    void deberiaRetornarConflictCuandoCodigoYaExiste() throws Exception {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "admin",
                "Propietaria",
                "Biofarma",
                "Clave1234",
                "ADMINISTRADORA"
        );

        when(registrarUsuarioUseCase.ejecutar(any(RegistrarUsuarioRequest.class)))
                .thenThrow(new CodigoUsuarioDuplicadoException("admin"));

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("admin")));
    }

    @Test
    void deberiaListarUsuariosCorrectamente() throws Exception {
        when(listarUsuariosUseCase.ejecutar()).thenReturn(List.of(
                new UsuarioResponse(1, "admin", "Propietaria", "Biofarma", "ADMINISTRADORA", true, LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoUsuario").value("admin"))
                .andExpect(jsonPath("$[0].rol").value("ADMINISTRADORA"));
    }
}