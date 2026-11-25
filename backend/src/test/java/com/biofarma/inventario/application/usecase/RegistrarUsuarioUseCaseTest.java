package com.biofarma.inventario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.biofarma.inventario.application.dto.RegistrarUsuarioRequest;
import com.biofarma.inventario.application.dto.UsuarioResponse;
import com.biofarma.inventario.application.mapper.UsuarioMapper;
import com.biofarma.inventario.domain.exception.CodigoUsuarioDuplicadoException;
import com.biofarma.inventario.domain.exception.DatosUsuarioInvalidosException;
import com.biofarma.inventario.domain.exception.RolNoEncontradoException;
import com.biofarma.inventario.domain.model.Rol;
import com.biofarma.inventario.domain.model.RolNombre;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.RolRepository;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import com.biofarma.inventario.domain.service.PasswordHasher;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegistrarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    private PasswordHasher passwordHasher;
    private RegistrarUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        passwordHasher = new PasswordHasher() {
            @Override
            public String hash(String rawPassword) {
                return encoder.encode(rawPassword);
            }

            @Override
            public boolean matches(String rawPassword, String encodedPassword) {
                return encoder.matches(rawPassword, encodedPassword);
            }
        };
        useCase = new RegistrarUsuarioUseCase(
                usuarioRepository,
                rolRepository,
                passwordHasher,
                new UsuarioMapper()
        );
    }

    @Test
    void deberiaRegistrarUsuarioConPasswordCifradaBCrypt() {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                " trab01 ",
                "Lucía",
                "Quispe Ramos",
                "ClaveSegura123",
                "TRABAJADORA"
        );
        Rol rolTrabajadora = new Rol(2, RolNombre.TRABAJADORA);

        when(usuarioRepository.existsByCodigoUsuario("trab01")).thenReturn(false);
        when(rolRepository.findByNombre(RolNombre.TRABAJADORA)).thenReturn(Optional.of(rolTrabajadora));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            return Usuario.reconstruir(
                    10,
                    u.getCodigoUsuario(),
                    u.getNombres(),
                    u.getApellidos(),
                    u.getPasswordHash(),
                    u.getRol(),
                    u.isActivo(),
                    LocalDateTime.now()
            );
        });

        UsuarioResponse response = useCase.ejecutar(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario usuarioGuardado = captor.getValue();

        assertEquals(10, response.idUsuario());
        assertEquals("trab01", response.codigoUsuario());
        assertEquals("TRABAJADORA", response.rol());
        assertTrue(response.activo());
        assertNotEquals("ClaveSegura123", usuarioGuardado.getPasswordHash());
        assertTrue(usuarioGuardado.getPasswordHash().startsWith("$2"));
        assertTrue(passwordHasher.matches("ClaveSegura123", usuarioGuardado.getPasswordHash()));
    }

    @Test
    void deberiaRechazarCodigoDuplicado() {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "admin",
                "Ana",
                "Flores",
                "ClaveSegura123",
                "ADMINISTRADORA"
        );

        when(usuarioRepository.existsByCodigoUsuario("admin")).thenReturn(true);

        assertThrows(CodigoUsuarioDuplicadoException.class, () -> useCase.ejecutar(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deberiaRechazarRolInvalido() {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "trab02",
                "Ana",
                "Flores",
                "ClaveSegura123",
                "CAJERA"
        );

        assertThrows(DatosUsuarioInvalidosException.class, () -> useCase.ejecutar(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deberiaFallarSiRolNoExisteEnBaseDeDatos() {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "trab03",
                "María",
                "Paredes",
                "ClaveSegura123",
                "TRABAJADORA"
        );

        when(usuarioRepository.existsByCodigoUsuario("trab03")).thenReturn(false);
        when(rolRepository.findByNombre(RolNombre.TRABAJADORA)).thenReturn(Optional.empty());

        assertThrows(RolNoEncontradoException.class, () -> useCase.ejecutar(request));
        verify(usuarioRepository, never()).save(any());
    }
}
