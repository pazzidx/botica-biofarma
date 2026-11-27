package com.biofarma.inventario.presentation.controller;

import com.biofarma.inventario.application.dto.RegistrarUsuarioRequest;
import com.biofarma.inventario.application.dto.UsuarioResponse;
import com.biofarma.inventario.application.usecase.CambiarEstadoUsuarioUseCase;
import com.biofarma.inventario.application.usecase.EliminarUsuarioUseCase;
import com.biofarma.inventario.application.usecase.ListarUsuariosUseCase;
import com.biofarma.inventario.application.usecase.RegistrarUsuarioUseCase;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestión y registro de usuarios.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final ListarUsuariosUseCase listarUsuariosUseCase;
    private final EliminarUsuarioUseCase eliminarUsuarioUseCase;
    private final CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase;

    public UsuarioController(
            RegistrarUsuarioUseCase registrarUsuarioUseCase,
            ListarUsuariosUseCase listarUsuariosUseCase,
            EliminarUsuarioUseCase eliminarUsuarioUseCase,
            CambiarEstadoUsuarioUseCase cambiarEstadoUsuarioUseCase
    ) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.listarUsuariosUseCase = listarUsuariosUseCase;
        this.eliminarUsuarioUseCase = eliminarUsuarioUseCase;
        this.cambiarEstadoUsuarioUseCase = cambiarEstadoUsuarioUseCase;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id, Authentication authentication) {
        String codigoAutenticado = authentication != null ? authentication.getName() : null;
        eliminarUsuarioUseCase.ejecutar(id, codigoAutenticado);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(@PathVariable Integer id, Authentication authentication) {
        String codigoAutenticado = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(cambiarEstadoUsuarioUseCase.ejecutar(id, codigoAutenticado));
    }
}