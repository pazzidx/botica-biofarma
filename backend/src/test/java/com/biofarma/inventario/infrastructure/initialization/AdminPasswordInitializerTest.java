package com.biofarma.inventario.infrastructure.initialization;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;

@ExtendWith(MockitoExtension.class)
class AdminPasswordInitializerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordHasher passwordHasher;

    private final Rol rolAdmin = new Rol(1, RolNombre.ADMINISTRADORA);

    @Test
    @DisplayName("Debería omitir la inicialización cuando la contraseña inicial está vacía o en blanco")
    void deberiaOmitirInicializacionCuandoPasswordInicialEstaEnBlanco() {
        AdminPasswordInitializer initializer = new AdminPasswordInitializer(
                usuarioRepository, passwordHasher, "   "
        );

        initializer.run(new DefaultApplicationArguments());

        verify(usuarioRepository, never()).findByCodigoUsuario(any());
        verify(passwordHasher, never()).hash(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería actualizar hash cuando el usuario admin existe con hash vacío o inválido")
    void deberiaActualizarHashCuandoAdminExisteConHashInvalido() {
        AdminPasswordInitializer initializer = new AdminPasswordInitializer(
                usuarioRepository, passwordHasher, "AdminNuevaPassword123*"
        );

        Usuario adminConHashVacio = Usuario.reconstruir(
                1, "admin", "Propietaria", "Biofarma", "", rolAdmin, true, LocalDateTime.now()
        );

        when(usuarioRepository.findByCodigoUsuario("admin")).thenReturn(Optional.of(adminConHashVacio));
        when(passwordHasher.hash("AdminNuevaPassword123*")).thenReturn("$2a$10$nuevoHashBCryptSeguro");

        initializer.run(new DefaultApplicationArguments());

        verify(usuarioRepository).findByCodigoUsuario("admin");
        verify(passwordHasher).hash("AdminNuevaPassword123*");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debería omitir actualización cuando el usuario admin ya tiene un hash válido")
    void deberiaOmitirActualizacionCuandoAdminTieneHashValido() {
        AdminPasswordInitializer initializer = new AdminPasswordInitializer(
                usuarioRepository, passwordHasher, "AdminNuevaPassword123*"
        );

        Usuario adminConHashValido = Usuario.reconstruir(
                1, "admin", "Propietaria", "Biofarma", "$2a$10$hashExistenteYaConfiguradoValido2026", rolAdmin, true, LocalDateTime.now()
        );

        when(usuarioRepository.findByCodigoUsuario("admin")).thenReturn(Optional.of(adminConHashValido));

        initializer.run(new DefaultApplicationArguments());

        verify(usuarioRepository).findByCodigoUsuario("admin");
        verify(passwordHasher, never()).hash(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debería omitir cuando el usuario admin no existe en la base de datos")
    void deberiaOmitirCuandoAdminNoExiste() {
        AdminPasswordInitializer initializer = new AdminPasswordInitializer(
                usuarioRepository, passwordHasher, "AdminPassword123*"
        );

        when(usuarioRepository.findByCodigoUsuario("admin")).thenReturn(Optional.empty());

        initializer.run(new DefaultApplicationArguments());

        verify(usuarioRepository).findByCodigoUsuario("admin");
        verify(passwordHasher, never()).hash(any());
        verify(usuarioRepository, never()).save(any());
    }
}
