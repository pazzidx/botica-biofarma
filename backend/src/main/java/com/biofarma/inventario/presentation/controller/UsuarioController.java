package com.biofarma.inventario.presentation.controller;

import com.biofarma.inventario.application.dto.ActualizarUsuarioRequest;
import com.biofarma.inventario.application.dto.EliminacionUsuarioResponse;
import com.biofarma.inventario.application.dto.RegistrarUsuarioRequest;
import com.biofarma.inventario.application.dto.UsuarioResponse;
import com.biofarma.inventario.application.usecase.ActivarUsuarioUseCase;
import com.biofarma.inventario.application.usecase.ActualizarUsuarioUseCase;
import com.biofarma.inventario.application.usecase.EliminarUsuarioUseCase;
import com.biofarma.inventario.application.usecase.ListarUsuariosUseCase;
import com.biofarma.inventario.application.usecase.RegistrarUsuarioUseCase;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST delgado para gestión y registro de usuarios (HU-01).
 * El control por rol (@PreAuthorize("hasRole('ADMINISTRADORA')")) se activará al integrar JWT en HU-02.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final ListarUsuariosUseCase listarUsuariosUseCase;
    private final ActualizarUsuarioUseCase actualizarUsuarioUseCase;
    private final EliminarUsuarioUseCase eliminarUsuarioUseCase;
    private final ActivarUsuarioUseCase activarUsuarioUseCase;

    public UsuarioController(
            RegistrarUsuarioUseCase registrarUsuarioUseCase,
            ListarUsuariosUseCase listarUsuariosUseCase,
            ActualizarUsuarioUseCase actualizarUsuarioUseCase,
            EliminarUsuarioUseCase eliminarUsuarioUseCase,
            ActivarUsuarioUseCase activarUsuarioUseCase
    ) {
        this.registrarUsuarioUseCase = registrarUsuarioUseCase;
        this.listarUsuariosUseCase = listarUsuariosUseCase;
        this.actualizarUsuarioUseCase = actualizarUsuarioUseCase;
        this.eliminarUsuarioUseCase = eliminarUsuarioUseCase;
        this.activarUsuarioUseCase = activarUsuarioUseCase;
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

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarUsuarioRequest request
    ) {
        return ResponseEntity.ok(actualizarUsuarioUseCase.ejecutar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<EliminacionUsuarioResponse> eliminar(@PathVariable Integer id) {
        return ResponseEntity.ok(eliminarUsuarioUseCase.ejecutar(id));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<UsuarioResponse> activar(@PathVariable Integer id) {
        return ResponseEntity.ok(activarUsuarioUseCase.ejecutar(id));
    }
}
