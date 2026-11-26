package com.biofarma.inventario.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    private final Rol rolAdmin = new Rol(1, RolNombre.ADMINISTRADORA);
    private final Rol rolTrabajadora = new Rol(2, RolNombre.TRABAJADORA);

    @Test
    @DisplayName("Debería retornar true en puedeAutenticarse() cuando el usuario está activo y tiene passwordHash válido")
    void deberiaPermitirAutenticacionCuandoUsuarioEstaActivoYTienePasswordHash() {
        Usuario usuario = Usuario.reconstruir(
                1,
                "admin",
                "Propietaria",
                "Biofarma",
                "$2a$10$hashBcryptValido12345678901234567890",
                rolAdmin,
                true,
                LocalDateTime.now()
        );

        assertThat(usuario.puedeAutenticarse()).isTrue();
    }

    @Test
    @DisplayName("Debería retornar false en puedeAutenticarse() cuando el usuario está inactivo")
    void deberiaDenegarAutenticacionCuandoUsuarioEstaInactivo() {
        Usuario usuario = Usuario.reconstruir(
                2,
                "c_mendoza",
                "Carla",
                "Mendoza",
                "$2a$10$hashBcryptValido12345678901234567890",
                rolTrabajadora,
                false, // Inactivo
                LocalDateTime.now()
        );

        assertThat(usuario.puedeAutenticarse()).isFalse();
    }

    @Test
    @DisplayName("Debería retornar false en puedeAutenticarse() cuando el passwordHash es vacío o en blanco")
    void deberiaDenegarAutenticacionCuandoPasswordHashEstaVacio() {
        Usuario usuarioConHashVacio = Usuario.reconstruir(
                3,
                "admin_incompleto",
                "Admin",
                "Incompleto",
                "", // Hash vacío
                rolAdmin,
                true,
                LocalDateTime.now()
        );

        Usuario usuarioConHashEspacios = Usuario.reconstruir(
                4,
                "admin_espacios",
                "Admin",
                "Espacios",
                "   ",
                rolAdmin,
                true,
                LocalDateTime.now()
        );

        assertThat(usuarioConHashVacio.puedeAutenticarse()).isFalse();
        assertThat(usuarioConHashEspacios.puedeAutenticarse()).isFalse();
    }
}
