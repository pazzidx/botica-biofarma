package com.biofarma.inventario.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.biofarma.inventario.application.dto.LogoutResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CerrarSesionUseCaseTest {

    private final CerrarSesionUseCase useCase = new CerrarSesionUseCase();

    @Test
    @DisplayName("Debería retornar confirmación exitosa con timestamp al cerrar sesión")
    void deberiaRetornarConfirmacionAlCerrarSesion() {
        LogoutResponse response = useCase.ejecutar();

        assertThat(response).isNotNull();
        assertThat(response.mensaje()).isEqualTo("Sesión cerrada exitosamente.");
        assertThat(response.timestamp()).isNotNull();
    }
}
