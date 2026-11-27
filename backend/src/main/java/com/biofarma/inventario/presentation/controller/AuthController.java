package com.biofarma.inventario.presentation.controller;

import com.biofarma.inventario.application.dto.LoginRequest;
import com.biofarma.inventario.application.dto.LoginResponse;
import com.biofarma.inventario.application.usecase.IniciarSesionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST delgado para autenticación de usuarios.
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final IniciarSesionUseCase iniciarSesionUseCase;

    public AuthController(IniciarSesionUseCase iniciarSesionUseCase) {
        this.iniciarSesionUseCase = iniciarSesionUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(iniciarSesionUseCase.ejecutar(request));
    }
}
