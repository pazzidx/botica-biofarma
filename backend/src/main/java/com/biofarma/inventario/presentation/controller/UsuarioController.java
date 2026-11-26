package com.biofarma.inventario.presentation.controller;

import com.biofarma.inventario.application.dto.RegistrarUsuarioRequest;
import com.biofarma.inventario.application.dto.UsuarioResponse;
import com.biofarma.inventario.application.usecase.ListarUsuariosUseCase;
import com.biofarma.inventario.application.usecase.RegistrarUsuarioUseCase;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST delgado para gestión y registro de usuarios (HU-01 / HU-02).
 * El acceso a todos sus endpoints está restringido exclusivamente al rol ADMINISTRADORA (BIO-31).
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasRole('ADMINISTRADORA')")
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final ListarUsuariosUseCase listarUsuariosUseCase;

    public UsuarioController(
            RegistrarUsuarioUseCase registrarUsuarioUseCase,
            ListarUsuariosUseCase listarUsuariosUseCase
    ) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.listarUsuariosUseCase = listarUsuariosUseCase;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistrarUsuarioRequest request) {
        UsuarioResponse response = registrarUsuarioUseCase.ejecutar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(listarUsuariosUseCase.ejecutar());
    }
}
