package com.biofarma.inventario.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.biofarma.inventario.application.dto.LoginRequest;
import com.biofarma.inventario.application.dto.LoginResponse;
import com.biofarma.inventario.application.service.TokenProvider;
import com.biofarma.inventario.domain.exception.CredencialesInvalidasException;
import com.biofarma.inventario.domain.model.Rol;
import com.biofarma.inventario.domain.model.RolNombre;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import com.biofarma.inventario.domain.service.PasswordHasher;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IniciarSesionUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private IniciarSesionUseCase useCase;

    private final Rol rolAdmin = new Rol(1, RolNombre.ADMINISTRADORA);
    private final Rol rolTrabajadora = new Rol(2, RolNombre.TRABAJADORA);

    @Test
    @DisplayName("Debería iniciar sesión exitosamente para usuario con rol ADMINISTRADORA")
    void deberiaIniciarSesionExitosamenteConAdministradora() {
        // Arrange
        LoginRequest request = new LoginRequest("admin", "Admin123*");
        Usuario admin = Usuario.reconstruir(
                1,
                "admin",
                "Propietaria",
                "Biofarma",
                "$2a$10$hashedPasswordAdmin",
                rolAdmin,
                true,
                LocalDateTime.now()
        );

        when(usuarioRepository.findByCodigoUsuario("admin")).thenReturn(Optional.of(admin));
        when(passwordHasher.matches("Admin123*", "$2a$10$hashedPasswordAdmin")).thenReturn(true);
        when(tokenProvider.generarToken(admin)).thenReturn("token.jwt.admin");
        when(tokenProvider.getExpiracionMs()).thenReturn(28800000L);

        // Act
        LoginResponse response = useCase.ejecutar(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("token.jwt.admin");
        assertThat(response.tipoToken()).isEqualTo("Bearer");
        assertThat(response.expiraEnMs()).isEqualTo(28800000L);
        assertThat(response.idUsuario()).isEqualTo(1);
        assertThat(response.codigoUsuario()).isEqualTo("admin");
        assertThat(response.nombres()).isEqualTo("Propietaria");
        assertThat(response.apellidos()).isEqualTo("Biofarma");
        assertThat(response.rol()).isEqualTo("ADMINISTRADORA");
        verify(tokenProvider).generarToken(admin);
    }

    @Test
    @DisplayName("Debería iniciar sesión exitosamente para usuario con rol TRABAJADORA")
    void deberiaIniciarSesionExitosamenteConTrabajadora() {
        // Arrange
        LoginRequest request = new LoginRequest("vendedora01", "ClaveSegura1");
        Usuario trabajadora = Usuario.reconstruir(
                2,
                "vendedora01",
                "Ana",
                "Torres",
                "$2a$10$hashedPasswordTrabajadora",
                rolTrabajadora,
                true,
                LocalDateTime.now()
        );

        when(usuarioRepository.findByCodigoUsuario("vendedora01")).thenReturn(Optional.of(trabajadora));
        when(passwordHasher.matches("ClaveSegura1", "$2a$10$hashedPasswordTrabajadora")).thenReturn(true);
        when(tokenProvider.generarToken(trabajadora)).thenReturn("token.jwt.trabajadora");
        when(tokenProvider.getExpiracionMs()).thenReturn(86400000L);

        // Act
        LoginResponse response = useCase.ejecutar(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("token.jwt.trabajadora");
        assertThat(response.rol()).isEqualTo("TRABAJADORA");
        assertThat(response.codigoUsuario()).isEqualTo("vendedora01");
    }

    @Test
    @DisplayName("Debería lanzar CredencialesInvalidasException cuando el código de usuario no existe")
    void deberiaLanzarExcepcionCuandoCodigoUsuarioNoExiste() {
        // Arrange
        LoginRequest request = new LoginRequest("noexiste", "Clave1234");
        when(usuarioRepository.findByCodigoUsuario("noexiste")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(request))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales inválidas.");

        verify(passwordHasher, never()).matches(any(), any());
        verify(tokenProvider, never()).generarToken(any());
    }

    @Test
    @DisplayName("Debería lanzar CredencialesInvalidasException cuando la contraseña es incorrecta")
    void deberiaLanzarExcepcionCuandoPasswordEsIncorrecto() {
        // Arrange
        LoginRequest request = new LoginRequest("admin", "ClaveErronea");
        Usuario admin = Usuario.reconstruir(
                1,
                "admin",
                "Propietaria",
                "Biofarma",
                "$2a$10$hashedPassword",
                rolAdmin,
                true,
                LocalDateTime.now()
        );

        when(usuarioRepository.findByCodigoUsuario("admin")).thenReturn(Optional.of(admin));
        when(passwordHasher.matches("ClaveErronea", "$2a$10$hashedPassword")).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(request))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales inválidas.");

        verify(tokenProvider, never()).generarToken(any());
    }

    @Test
    @DisplayName("Debería lanzar CredencialesInvalidasException cuando el usuario está inactivo")
    void deberiaLanzarExcepcionCuandoUsuarioEstaInactivo() {
        // Arrange
        LoginRequest request = new LoginRequest("trabajadoraInactiva", "Clave1234");
        Usuario inactivo = Usuario.reconstruir(
                3,
                "trabajadoraInactiva",
                "Rosa",
                "Perez",
                "$2a$10$hashedPassword",
                rolTrabajadora,
                false, // Inactivo
                LocalDateTime.now()
        );

        when(usuarioRepository.findByCodigoUsuario("trabajadoraInactiva")).thenReturn(Optional.of(inactivo));

        // Act & Assert
        assertThatThrownBy(() -> useCase.ejecutar(request))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales inválidas.");

        verify(passwordHasher, never()).matches(any(), any());
        verify(tokenProvider, never()).generarToken(any());
    }

    @Test
    @DisplayName("Debería lanzar CredencialesInvalidasException cuando la solicitud contiene campos nulos")
    void deberiaLanzarExcepcionCuandoRequestTieneCamposNulos() {
        assertThatThrownBy(() -> useCase.ejecutar(null))
                .isInstanceOf(CredencialesInvalidasException.class);

        assertThatThrownBy(() -> useCase.ejecutar(new LoginRequest(null, "123456")))
                .isInstanceOf(CredencialesInvalidasException.class);

        assertThatThrownBy(() -> useCase.ejecutar(new LoginRequest("admin", null)))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    @DisplayName("Debería lanzar CredencialesInvalidasException y no consultar repositorio cuando el código o password están en blanco")
    void deberiaLanzarExcepcionYNoConsultarRepositorioCuandoCredencialesEstanEnBlanco() {
        // Arrange
        LoginRequest requestCodigoBlanco = new LoginRequest("   ", "password123");
        LoginRequest requestPasswordBlanco = new LoginRequest("admin", "   ");
        LoginRequest requestAmbosBlanco = new LoginRequest("", "");

        // Act & Assert - Código en blanco
        assertThatThrownBy(() -> useCase.ejecutar(requestCodigoBlanco))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales inválidas.");

        // Act & Assert - Password en blanco
        assertThatThrownBy(() -> useCase.ejecutar(requestPasswordBlanco))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales inválidas.");

        // Act & Assert - Ambos en blanco
        assertThatThrownBy(() -> useCase.ejecutar(requestAmbosBlanco))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales inválidas.");

        verify(usuarioRepository, never()).findByCodigoUsuario(any());
        verify(passwordHasher, never()).matches(any(), any());
        verify(tokenProvider, never()).generarToken(any());
    }
}
