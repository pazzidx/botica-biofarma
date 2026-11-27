package com.biofarma.inventario.application.usecase;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.biofarma.inventario.application.dto.UsuarioResponse;
import com.biofarma.inventario.application.mapper.UsuarioMapper;
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
class ActivarUsuarioUseCaseTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private ActivarUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ActivarUsuarioUseCase(usuarioRepository, new UsuarioMapper());
    }

    @Test
    void deberiaReactivarUsuarioInactivo() {
        Usuario inactiva = Usuario.reconstruir(
                3,
                "trab02",
                "Rosa",
                "Díaz",
                "$2a$10$hash",
                new Rol(2, RolNombre.TRABAJADORA),
                false,
                LocalDateTime.now()
        );
        when(usuarioRepository.findById(3)).thenReturn(Optional.of(inactiva));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse response = useCase.ejecutar(3);

        assertTrue(response.activo());
        verify(usuarioRepository).save(any(Usuario.class));
    }
}
