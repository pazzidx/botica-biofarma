package com.biofarma.inventario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.biofarma.inventario.application.dto.EliminacionUsuarioResponse;
import com.biofarma.inventario.application.mapper.UsuarioMapper;
import com.biofarma.inventario.domain.exception.OperacionNoPermitidaException;
import com.biofarma.inventario.domain.model.Rol;
import com.biofarma.inventario.domain.model.RolNombre;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class EliminarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private EliminarUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new EliminarUsuarioUseCase(usuarioRepository, new UsuarioMapper());
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deberiaEliminarFisicamenteCuandoTieneCeroVentas() {
        Usuario trabajadora = Usuario.reconstruir(
                2,
                "trab01",
                "Lucía",
                "Quispe",
                "$2a$10$hash",
                new Rol(2, RolNombre.TRABAJADORA),
                true,
                LocalDateTime.now()
        );
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(trabajadora));
        when(usuarioRepository.countVentasByUsuarioId(2)).thenReturn(0L);

        EliminacionUsuarioResponse response = useCase.ejecutar(2);

        assertTrue(response.eliminadoFisicamente());
        assertEquals(2, response.idUsuario());
        assertNull(response.usuario());
        verify(usuarioRepository).deleteById(2);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deberiaDesactivarLogicamenteCuandoTieneUnaOMasVentas() {
        Usuario trabajadora = Usuario.reconstruir(
                3,
                "trab02",
                "Rosa",
                "Díaz",
                "$2a$10$hash",
                new Rol(2, RolNombre.TRABAJADORA),
                true,
                LocalDateTime.now()
        );
        when(usuarioRepository.findById(3)).thenReturn(Optional.of(trabajadora));
        when(usuarioRepository.countVentasByUsuarioId(3)).thenReturn(4L);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        EliminacionUsuarioResponse response = useCase.ejecutar(3);

        assertFalse(response.eliminadoFisicamente());
        assertNotNull(response.usuario());
        assertFalse(response.usuario().activo());
        verify(usuarioRepository, never()).deleteById(any());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deberiaBloquearEliminacionDeCuentaAdmin() {
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

        assertThrows(OperacionNoPermitidaException.class, () -> useCase.ejecutar(1));
        verify(usuarioRepository, never()).deleteById(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deberiaBloquearEliminacionDeSesionActual() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("trab01", "n/a", List.of())
        );
        Usuario actual = Usuario.reconstruir(
                5,
                "trab01",
                "Lucía",
                "Quispe",
                "$2a$10$hash",
                new Rol(2, RolNombre.TRABAJADORA),
                true,
                LocalDateTime.now()
        );
        when(usuarioRepository.findById(5)).thenReturn(Optional.of(actual));

        assertThrows(OperacionNoPermitidaException.class, () -> useCase.ejecutar(5));
        verify(usuarioRepository, never()).deleteById(any());
    }
}
