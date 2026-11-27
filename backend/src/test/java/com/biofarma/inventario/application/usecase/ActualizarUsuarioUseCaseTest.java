package com.biofarma.inventario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.biofarma.inventario.application.dto.ActualizarUsuarioRequest;
import com.biofarma.inventario.application.dto.UsuarioResponse;
import com.biofarma.inventario.application.mapper.UsuarioMapper;
import com.biofarma.inventario.domain.exception.DatosUsuarioInvalidosException;
import com.biofarma.inventario.domain.exception.OperacionNoPermitidaException;
import com.biofarma.inventario.domain.exception.UsuarioNoEncontradoException;
import com.biofarma.inventario.domain.model.Rol;
import com.biofarma.inventario.domain.model.RolNombre;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActualizarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private ActualizarUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ActualizarUsuarioUseCase(usuarioRepository, new UsuarioMapper());
    }

    @Test
    void deberiaActualizarNombresYApellidosDeTrabajadora() {
        Usuario existente = Usuario.reconstruir(
                2,
                "trab01",
                "Carla",
                "Mendoza",
                "$2a$10$hash",
                new Rol(2, RolNombre.TRABAJADORA),
                true,
                LocalDateTime.now()
        );
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse response = useCase.ejecutar(2, new ActualizarUsuarioRequest("Carla María", "Mendoza Pérez"));

        assertEquals("Carla María", response.nombres());
        assertEquals("Mendoza Pérez", response.apellidos());
        assertEquals("trab01", response.codigoUsuario());
        assertEquals("TRABAJADORA", response.rol());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deberiaBloquearEdicionDeCuentaAdmin() {
        Usuario admin = Usuario.reconstruir(
                1,
                "admin",
                "Propietaria",
                "Biofarma",
                "$2a$10$hash",
                new Rol(1, RolNombre.ADMINISTRADORA),
                true,
                LocalDateTime.now()
        );
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(admin));

        assertThrows(
                OperacionNoPermitidaException.class,
                () -> useCase.ejecutar(1, new ActualizarUsuarioRequest("Nuevo", "Nombre"))
        );
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deberiaRechazarNumerosOSimbolosEnEdicion() {
        Usuario existente = Usuario.reconstruir(
                2,
                "trab01",
                "Carla",
                "Mendoza",
                "$2a$10$hash",
                new Rol(2, RolNombre.TRABAJADORA),
                true,
                LocalDateTime.now()
        );
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(existente));

        assertThrows(
                DatosUsuarioInvalidosException.class,
                () -> useCase.ejecutar(2, new ActualizarUsuarioRequest("Carla123", "Mendoza!"))
        );
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deberiaLanzarNotFoundSiUsuarioNoExiste() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(
                UsuarioNoEncontradoException.class,
                () -> useCase.ejecutar(99, new ActualizarUsuarioRequest("Carla", "Mendoza"))
        );
    }
}
