package com.biofarma.inventario.infrastructure.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.biofarma.inventario.application.dto.RegistrarUsuarioRequest;
import com.biofarma.inventario.application.usecase.ActivarUsuarioUseCase;
import com.biofarma.inventario.application.usecase.ActualizarUsuarioUseCase;
import com.biofarma.inventario.application.usecase.EliminarUsuarioUseCase;
import com.biofarma.inventario.application.usecase.ListarUsuariosUseCase;
import com.biofarma.inventario.application.usecase.RegistrarUsuarioUseCase;
import com.biofarma.inventario.domain.model.Rol;
import com.biofarma.inventario.domain.model.RolNombre;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import com.biofarma.inventario.presentation.advice.GlobalExceptionHandler;
import com.biofarma.inventario.presentation.controller.UsuarioController;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = UsuarioController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AutorizacionRolesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @MockitoBean
    private RegistrarUsuarioUseCase registrarUsuarioUseCase;

    @MockitoBean
    private ListarUsuariosUseCase listarUsuariosUseCase;

    @MockitoBean
    private ActualizarUsuarioUseCase actualizarUsuarioUseCase;

    @MockitoBean
    private EliminarUsuarioUseCase eliminarUsuarioUseCase;

    @MockitoBean
    private ActivarUsuarioUseCase activarUsuarioUseCase;

    private final Rol rolAdmin = new Rol(1, RolNombre.ADMINISTRADORA);
    private final Rol rolTrabajadora = new Rol(2, RolNombre.TRABAJADORA);

    @Test
    @DisplayName("Debería permitir acceso a /api/v1/usuarios con token cuando en persistencia el rol es ADMINISTRADORA")
    void deberiaPermitirAccesoAUsuariosCuandoRolEnPersistenciaEsAdministradora() throws Exception {
        Usuario admin = Usuario.reconstruir(
                1,
                "admin",
                "Propietaria",
                "Biofarma",
                "$2a$10$hashed",
                rolAdmin,
                true,
                LocalDateTime.now()
        );

        String token = jwtService.generarToken(admin);
        when(usuarioRepository.findByCodigoUsuario("admin")).thenReturn(Optional.of(admin));

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debería denegar acceso con 403 Forbidden a /api/v1/usuarios con token cuando en persistencia el rol es TRABAJADORA")
    void deberiaDenegarAccesoAUsuariosCuandoRolEnPersistenciaEsTrabajadora() throws Exception {
        Usuario trabajadora = Usuario.reconstruir(
                2,
                "trabajadora01",
                "Ana",
                "Torres",
                "$2a$10$hashed",
                rolTrabajadora,
                true,
                LocalDateTime.now()
        );

        String token = jwtService.generarToken(trabajadora);
        when(usuarioRepository.findByCodigoUsuario("trabajadora01")).thenReturn(Optional.of(trabajadora));

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acceso denegado"))
                .andExpect(jsonPath("$.message").value("No tiene permisos suficientes para realizar esta operación."));
    }

    @Test
    @DisplayName("Debería reflejar cambio de rol en persistencia: token emitido con ADMINISTRADORA pero cambiado a TRABAJADORA recibe 403 Forbidden")
    void deberiaReflejarCambioDeRolEnPersistenciaAunqueTokenContengaRolAnterior() throws Exception {
        // 1. Token emitido originalmente cuando el usuario tenía rol ADMINISTRADORA en el claim del JWT
        Usuario usuarioConRolAntiguo = Usuario.reconstruir(
                3,
                "usuarioDegradado",
                "Carla",
                "Rios",
                "$2a$10$hashed",
                rolAdmin, // Emitido como ADMIN
                true,
                LocalDateTime.now()
        );
        String tokenAntiguoAdmin = jwtService.generarToken(usuarioConRolAntiguo);

        // 2. En la base de datos (persistencia), el rol del usuario fue actualizado a TRABAJADORA
        Usuario usuarioActualizadoEnBD = Usuario.reconstruir(
                3,
                "usuarioDegradado",
                "Carla",
                "Rios",
                "$2a$10$hashed",
                rolTrabajadora, // Ahora es TRABAJADORA en persistencia
                true,
                LocalDateTime.now()
        );
        when(usuarioRepository.findByCodigoUsuario("usuarioDegradado")).thenReturn(Optional.of(usuarioActualizadoEnBD));

        // 3. Intenta acceder a gestión de usuarios con el token que decía "ADMINISTRADORA":
        // El filtro verifica en persistencia, aplica ROLE_TRABAJADORA y deniega el acceso con 403 Forbidden
        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenAntiguoAdmin))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Acceso denegado"));
    }

    @Test
    @DisplayName("Debería denegar acceso con 401 Unauthorized a /api/v1/usuarios cuando el token está expirado")
    void deberiaDenegarAccesoCuandoTokenEstaExpirado() throws Exception {
        JwtService expiredService = new JwtService(
                "ClaveSecretaDePruebaParaTestingUnitarioEIntegracionDe256BitsMinimo!",
                -1000L
        );
        Usuario admin = Usuario.reconstruir(
                1,
                "admin",
                "Propietaria",
                "Biofarma",
                "$2a$10$hashed",
                rolAdmin,
                true,
                LocalDateTime.now()
        );
        String tokenExpirado = expiredService.generarToken(admin);

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenExpirado))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("No autorizado"));
    }

    @Test
    @DisplayName("Debería denegar acceso con 401 Unauthorized a /api/v1/usuarios cuando el token es alterado o corrupto")
    void deberiaDenegarAccesoCuandoTokenEstaAlterado() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.token.alterado.invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("No autorizado"));
    }

    @Test
    @DisplayName("Debería denegar acceso con 401 Unauthorized cuando el usuario tiene token válido pero fue desactivado en persistencia")
    void deberiaDenegarAccesoConTokenValidoSiUsuarioFueDesactivadoEnPersistencia() throws Exception {
        Usuario admin = Usuario.reconstruir(
                1,
                "admin",
                "Propietaria",
                "Biofarma",
                "$2a$10$hashed",
                rolAdmin,
                true,
                LocalDateTime.now()
        );
        String tokenValido = jwtService.generarToken(admin);

        // En BD ahora está desactivado:
        Usuario adminDesactivado = Usuario.reconstruir(
                1,
                "admin",
                "Propietaria",
                "Biofarma",
                "$2a$10$hashed",
                rolAdmin,
                false, // INACTIVO
                LocalDateTime.now()
        );
        when(usuarioRepository.findByCodigoUsuario("admin")).thenReturn(Optional.of(adminDesactivado));

        mockMvc.perform(get("/api/v1/usuarios")
                        .header("Authorization", "Bearer " + tokenValido))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("No autorizado"));
    }
}
