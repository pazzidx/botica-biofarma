package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.LogoutResponse;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/**
 * Caso de uso: Cierre de sesión de usuario autenticado (HU-02 / BIO-30).
 * En la arquitectura JWT sin estado, confirma el cierre en el servidor y prepara
 * el descarte del token y redirección en el cliente (BIO-32).
 */
@Service
public class CerrarSesionUseCase {

    public LogoutResponse ejecutar() {
        return new LogoutResponse(
                "Sesión cerrada exitosamente.",
                LocalDateTime.now()
        );
    }
}
