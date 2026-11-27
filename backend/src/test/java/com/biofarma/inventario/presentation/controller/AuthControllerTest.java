package com.biofarma.inventario.presentation.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.biofarma.inventario.application.dto.LoginRequest;
import com.biofarma.inventario.application.dto.LoginResponse;
import com.biofarma.inventario.application.dto.LogoutResponse;
import com.biofarma.inventario.application.usecase.CerrarSesionUseCase;
import com.biofarma.inventario.application.usecase.IniciarSesionUseCase;
import com.biofarma.inventario.domain.exception.CredencialesInvalidasException;
import com.biofarma.inventario.domain.model.Rol;
import com.biofarma.inventario.domain.model.RolNombre;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import com.biofarma.inventario.infrastructure.security.JwtService;
import com.biofarma.inventario.infrastructure.security.SecurityConfig;
import com.biofarma.inventario.presentation.advice.GlobalExceptionHandler;
import java.time.LocalDateTime;
import java.util.Optional;
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

@WebMvcTest(controllers = AuthController.class)
@Import({ SecurityConfig.class, GlobalExceptionHandler.class })
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private JwtService jwtService;

        @MockitoBean
        private IniciarSesionUseCase iniciarSesionUseCase;

        @MockitoBean
        private CerrarSesionUseCase cerrarSesionUseCase;

        @MockitoBean
        private UsuarioRepository usuarioRepository;

        @Test
        @DisplayName("Debería retornar 200 OK y token con vigencia de 8 horas y datos públicos sin contraseñas ni hashes")
        void deberiaRetornar200YTokenEnLoginExitoso() throws Exception {
                LoginRequest request = new LoginRequest("admin", "Admin123*");
                LoginResponse response = new LoginResponse(
                                "token.jwt.valido.mock",
                                "Bearer",
                                28800000L, // 8 horas
                                1,
                                "admin",
                                "Propietaria",
                                "Biofarma",
                                "ADMINISTRADORA");

                when(iniciarSesionUseCase.ejecutar(any(LoginRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("token.jwt.valido.mock"))
                                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                                .andExpect(jsonPath("$.expiraEnMs").value(28800000L))
                                .andExpect(jsonPath("$.codigoUsuario").value("admin"))
                                .andExpect(jsonPath("$.nombres").value("Propietaria"))
                                .andExpect(jsonPath("$.apellidos").value("Biofarma"))
                                .andExpect(jsonPath("$.rol").value("ADMINISTRADORA"))
                                .andExpect(content().string(not(containsString("password"))))
                                .andExpect(content().string(not(containsString("passwordHash"))))
                                .andExpect(content().string(not(containsString("Admin123*"))));
        }

        @Test
        @DisplayName("Debería retornar 400 Bad Request cuando la solicitud de login tiene campos obligatorios vacíos")
        void deberiaRetornar400CuandoLoginTieneCamposVacios() throws Exception {
                LoginRequest requestInvalido = new LoginRequest("", "");

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestInvalido)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value(400))
                                .andExpect(jsonPath("$.fieldErrors.codigoUsuario").exists())
                                .andExpect(jsonPath("$.fieldErrors.password").exists());
        }

        @Test
        @DisplayName("Debería retornar 401 Unauthorized con mensaje claro cuando las credenciales son inválidas")
        void deberiaRetornar401CuandoCredencialesSonInvalidas() throws Exception {
                LoginRequest request = new LoginRequest("usuarioInvalido", "claveErronea");

                when(iniciarSesionUseCase.ejecutar(any(LoginRequest.class)))
                                .thenThrow(new CredencialesInvalidasException("Credenciales inválidas."));

                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.error").value("Credenciales inválidas"))
                                .andExpect(jsonPath("$.message")
                                                .value("El código de usuario o la contraseña son incorrectos."));
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMINISTRADORA" })
        @DisplayName("Debería permitir cierre de sesión con 200 OK cuando el usuario está autenticado")
        void deberiaPermitirLogoutCuandoUsuarioEstaAutenticado() throws Exception {
                when(cerrarSesionUseCase.ejecutar()).thenReturn(
                                new LogoutResponse("Sesión cerrada exitosamente.", LocalDateTime.now()));

                mockMvc.perform(post("/api/v1/auth/logout"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.mensaje").value("Sesión cerrada exitosamente."))
                                .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Debería rechazar cierre de sesión con 401 Unauthorized cuando el usuario no está autenticado")
        void deberiaRechazarLogoutSinAutenticacion() throws Exception {
                mockMvc.perform(post("/api/v1/auth/logout"))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.error").value("No autorizado"));
        }

        @Test
        @DisplayName("Debería revocar acceso (401) cuando el usuario posee un token válido pero fue desactivado en base de datos")
        void deberiaRechazarAccesoConTokenValidoSiUsuarioFueDesactivadoEnBaseDeDatos() throws Exception {
                // Arrange
                Rol rol = new Rol(2, RolNombre.TRABAJADORA);
                Usuario usuarioActivoOriginal = Usuario.reconstruir(
                                5,
                                "trabajadoraDesactivada",
                                "Elena",
                                "Castro",
                                "$2a$10$hashed",
                                rol,
                                true,
                                LocalDateTime.now());

                // Genera token válido
                String tokenValido = jwtService.generarToken(usuarioActivoOriginal);

                // Pero en la base de datos, el usuario ahora se encuentra inactivo (activo =
                // false)
                Usuario usuarioInactivo = Usuario.reconstruir(
                                5,
                                "trabajadoraDesactivada",
                                "Elena",
                                "Castro",
                                "$2a$10$hashed",
                                rol,
                                false, // DESACTIVADO
                                LocalDateTime.now());
                when(usuarioRepository.findByCodigoUsuario("trabajadoraDesactivada"))
                                .thenReturn(Optional.of(usuarioInactivo));

                // Act & Assert
                mockMvc.perform(post("/api/v1/auth/logout")
                                .header("Authorization", "Bearer " + tokenValido))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.status").value(401))
                                .andExpect(jsonPath("$.error").value("No autorizado"));
        }

        @Test
        @DisplayName("Debería permitir acceso al recurso protegido cuando el usuario posee token válido y está activo en base de datos")
        void deberiaPermitirAccesoCuandoUsuarioTieneTokenValidoYEstaActivo() throws Exception {
                Rol rol = new Rol(1, RolNombre.ADMINISTRADORA);
                Usuario adminActivo = Usuario.reconstruir(
                                1,
                                "admin",
                                "Propietaria",
                                "Biofarma",
                                "$2a$10$hashed",
                                rol,
                                true,
                                LocalDateTime.now());

                String tokenValido = jwtService.generarToken(adminActivo);

                when(usuarioRepository.findByCodigoUsuario("admin")).thenReturn(Optional.of(adminActivo));
                when(cerrarSesionUseCase.ejecutar()).thenReturn(
                                new LogoutResponse("Sesión cerrada exitosamente.", LocalDateTime.now()));

                mockMvc.perform(post("/api/v1/auth/logout")
                                .header("Authorization", "Bearer " + tokenValido))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.mensaje").value("Sesión cerrada exitosamente."));
        }
}
