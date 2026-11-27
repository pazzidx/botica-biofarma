package com.biofarma.inventario.presentation.controller;

import com.biofarma.inventario.application.dto.LoginRequest;
import com.biofarma.inventario.application.dto.LoginResponse;
import com.biofarma.inventario.application.dto.LogoutResponse;
import com.biofarma.inventario.application.usecase.CerrarSesionUseCase;
import com.biofarma.inventario.application.usecase.IniciarSesionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST delgado para inicio y cierre de sesión (HU-02 / BIO-30).
 * No contiene lógica de negocio; orquesta a través de los casos de uso de aplicación.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final IniciarSesionUseCase iniciarSesionUseCase;
    private final CerrarSesionUseCase cerrarSesionUseCase;

    public AuthController(
            IniciarSesionUseCase iniciarSesionUseCase,
            CerrarSesionUseCase cerrarSesionUseCase
    ) {
        this.iniciarSesionUseCase = iniciarSesionUseCase;
        this.cerrarSesionUseCase = cerrarSesionUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = iniciarSesionUseCase.ejecutar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout() {
        LogoutResponse response = cerrarSesionUseCase.ejecutar();
        return ResponseEntity.ok(response);
    }
}
