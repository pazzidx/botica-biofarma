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
import com.biofarma.inventario.application.usecase.ListarUsuariosUseCase;
import com.biofarma.inventario.application.usecase.RegistrarUsuarioUseCase;
import com.biofarma.inventario.domain.exception.CodigoUsuarioDuplicadoException;
import com.biofarma.inventario.infrastructure.security.SecurityConfig;
import com.biofarma.inventario.presentation.advice.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
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
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @MockitoBean
    private ListarUsuariosUseCase listarUsuariosUseCase;

    // --- PRUEBAS FUNCIONALES DE HU-01 (Autorizadas como ADMINISTRADORA) ---

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADORA"})
    @DisplayName("Debería registrar usuario y no exponer password cuando lo ejecuta ADMINISTRADORA")
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
    @WithMockUser(username = "admin", roles = {"ADMINISTRADORA"})
    @DisplayName("Debería retornar BadRequest cuando faltan campos al registrar")
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
    @WithMockUser(username = "admin", roles = {"ADMINISTRADORA"})
    @DisplayName("Debería retornar Conflict cuando código de usuario ya existe")
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
    @WithMockUser(username = "admin", roles = {"ADMINISTRADORA"})
    @DisplayName("Debería listar usuarios correctamente cuando lo solicita ADMINISTRADORA")
    void deberiaListarUsuariosCorrectamente() throws Exception {
        when(listarUsuariosUseCase.ejecutar()).thenReturn(List.of(
                new UsuarioResponse(1, "admin", "Propietaria", "Biofarma", "ADMINISTRADORA", true, LocalDateTime.now())
        ));

        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoUsuario").value("admin"))
                .andExpect(jsonPath("$[0].rol").value("ADMINISTRADORA"));
    }

    // --- PRUEBAS DE AUTORIZACIÓN Y BLOQUEO POR ROL (BIO-31) ---

    @Test
    @WithMockUser(username = "trabajadora01", roles = {"TRABAJADORA"})
    @DisplayName("Debería rechazar registro de usuario con 403 Forbidden cuando el rol es TRABAJADORA")
    void deberiaRechazarRegistroCuandoUsuarioEsTrabajadora() throws Exception {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "nuevoUser",
                "Nombre",
                "Apellido",
                "Clave1234",
                "TRABAJADORA"
        );

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acceso denegado"))
                .andExpect(jsonPath("$.message").value("No tiene permisos suficientes para realizar esta operación."));
    }

    @Test
    @WithMockUser(username = "trabajadora01", roles = {"TRABAJADORA"})
    @DisplayName("Debería rechazar listado de usuarios con 403 Forbidden cuando el rol es TRABAJADORA")
    void deberiaRechazarListadoCuandoUsuarioEsTrabajadora() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acceso denegado"))
                .andExpect(jsonPath("$.message").value("No tiene permisos suficientes para realizar esta operación."));
    }

    @Test
    @DisplayName("Debería rechazar registro de usuario con 401 Unauthorized cuando no existe autenticación")
    void deberiaRechazarRegistroSinAutenticacion() throws Exception {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "nuevoUser",
                "Nombre",
                "Apellido",
                "Clave1234",
                "TRABAJADORA"
        );

        mockMvc.perform(post("/api/v1/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("No autorizado"))
                .andExpect(jsonPath("$.message").value("Debe iniciar sesión para acceder a este recurso."));
    }

    @Test
    @DisplayName("Debería rechazar listado de usuarios con 401 Unauthorized cuando no existe autenticación")
    void deberiaRechazarListadoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("No autorizado"))
                .andExpect(jsonPath("$.message").value("Debe iniciar sesión para acceder a este recurso."));
    }
}
