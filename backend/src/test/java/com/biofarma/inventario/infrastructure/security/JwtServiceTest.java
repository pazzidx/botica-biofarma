package com.biofarma.inventario.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.biofarma.inventario.domain.model.Rol;
import com.biofarma.inventario.domain.model.RolNombre;
import com.biofarma.inventario.domain.model.Usuario;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secretKey = "Biofarma2026ClaveSecretaSuperSeguraParaTokensJWT256Bits!";
    private final long expirationMs = 28800000L; // 8 horas

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secretKey, expirationMs);
    }

    @Test
    @DisplayName("Debería generar token válido y extraer claims correctamente")
    void deberiaGenerarTokenValidoYExtraerClaims() {
        // Arrange
        Rol rol = new Rol(1, RolNombre.ADMINISTRADORA);
        Usuario usuario = Usuario.reconstruir(
                1,
                "admin",
                "Propietaria",
                "Biofarma",
                "$2a$10$hashed",
                rol,
                true,
                LocalDateTime.now()
        );

        // Act
        String token = jwtService.generarToken(usuario);

        // Assert
        assertThat(token).isNotBlank();
        assertThat(jwtService.esTokenValido(token)).isTrue();
        assertThat(jwtService.esTokenValido(token, "admin")).isTrue();
        assertThat(jwtService.esTokenValido(token, "otroUsuario")).isFalse();
        assertThat(jwtService.extraerCodigoUsuario(token)).isEqualTo("admin");
        assertThat(jwtService.extraerRol(token)).isEqualTo("ADMINISTRADORA");
        assertThat(jwtService.getExpiracionMs()).isEqualTo(expirationMs);
    }

    @Test
    @DisplayName("Debería rechazar token manipulado o corrupto")
    void deberiaRechazarTokenInvalido() {
        assertThat(jwtService.esTokenValido("token.invalido.corrupto")).isFalse();
        assertThat(jwtService.esTokenValido("")).isFalse();
        assertThat(jwtService.esTokenValido(null)).isFalse();
    }

    @Test
    @DisplayName("Debería rechazar token expirado")
    void deberiaRechazarTokenExpirado() {
        JwtService expiredService = new JwtService(secretKey, -1000);
        Rol rol = new Rol(2, RolNombre.TRABAJADORA);
        Usuario usuario = Usuario.reconstruir(
                2,
                "trabajadora01",
                "Maria",
                "Lopez",
                "$2a$10$hashed",
                rol,
                true,
                LocalDateTime.now()
        );

        String expiredToken = expiredService.generarToken(usuario);

        assertThat(jwtService.esTokenValido(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException cuando la clave es nula, vacía o menor a 256 bits")
    void deberiaRechazarClaveInseguraOInvalida() {
        assertThatThrownBy(() -> new JwtService(null, 1000L))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new JwtService("   ", 1000L))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> new JwtService("claveCortaDeMenosDe32Bytes", 1000L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Debería extraer fecha de expiración correctamente desde token válido")
    void deberiaExtraerFechaExpiracionCorrectamenteDesdeTokenValido() {
        // Arrange
        Rol rol = new Rol(1, RolNombre.ADMINISTRADORA);
        Usuario usuario = Usuario.reconstruir(
                1,
                "admin",
                "Propietaria",
                "Biofarma",
                "$2a$10$hashed",
                rol,
                true,
                LocalDateTime.now()
        );
        long antesGenerar = System.currentTimeMillis();

        // Act
        String token = jwtService.generarToken(usuario);
        java.util.Date fechaExpiracion = jwtService.extraerFechaExpiracion(token);

        // Assert
        assertThat(fechaExpiracion).isNotNull();
        assertThat(fechaExpiracion.getTime()).isGreaterThan(antesGenerar);
        assertThat(fechaExpiracion.getTime()).isLessThanOrEqualTo(System.currentTimeMillis() + expirationMs + 1000L);
    }

    @Test
    @DisplayName("Debería fallar al extraer fecha de expiración de token inválido o corrupto")
    void deberiaFallarAlExtraerFechaExpiracionDeTokenInvalido() {
        assertThatThrownBy(() -> jwtService.extraerFechaExpiracion("token.falso.corrupto"))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);

        assertThatThrownBy(() -> jwtService.extraerFechaExpiracion(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
