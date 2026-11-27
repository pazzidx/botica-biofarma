package com.biofarma.inventario.presentation.controller;

import com.biofarma.inventario.application.dto.ActualizarMedicamentoRequest;
import com.biofarma.inventario.application.dto.MedicamentoResponse;
import com.biofarma.inventario.application.dto.RegistrarMedicamentoRequest;
import com.biofarma.inventario.application.usecase.ActualizarMedicamentoUseCase;
import com.biofarma.inventario.application.usecase.DesactivarMedicamentoUseCase;
import com.biofarma.inventario.application.usecase.ListarMedicamentosUseCase;
import com.biofarma.inventario.application.usecase.RegistrarMedicamentoUseCase;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST delgado para la administración del catálogo de medicamentos (HU-04).
 */
@RestController
@RequestMapping("/api/v1/medicamentos")
public class MedicamentoController {

    private final RegistrarMedicamentoUseCase registrarMedicamentoUseCase;
    private final ActualizarMedicamentoUseCase actualizarMedicamentoUseCase;
    private final DesactivarMedicamentoUseCase desactivarMedicamentoUseCase;
    private final ListarMedicamentosUseCase listarMedicamentosUseCase;

    public MedicamentoController(
            RegistrarMedicamentoUseCase registrarMedicamentoUseCase,
            ActualizarMedicamentoUseCase actualizarMedicamentoUseCase,
            DesactivarMedicamentoUseCase desactivarMedicamentoUseCase,
            ListarMedicamentosUseCase listarMedicamentosUseCase
    ) {
        this.registrarMedicamentoUseCase = registrarMedicamentoUseCase;
        this.actualizarMedicamentoUseCase = actualizarMedicamentoUseCase;
        this.desactivarMedicamentoUseCase = desactivarMedicamentoUseCase;
        this.listarMedicamentosUseCase = listarMedicamentosUseCase;
    }

    @GetMapping
    public ResponseEntity<List<MedicamentoResponse>> listar(
            @RequestParam(name = "soloActivos", required = false) Boolean soloActivos,
            Authentication authentication
    ) {
        boolean esAdministradora = authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_ADMINISTRADORA".equals(authority.getAuthority()));
        Boolean filtroActivos = esAdministradora ? soloActivos : Boolean.TRUE;
        return ResponseEntity.ok(listarMedicamentosUseCase.ejecutar(filtroActivos));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<MedicamentoResponse> registrar(
            @Valid @RequestBody RegistrarMedicamentoRequest request
    ) {
        MedicamentoResponse response = registrarMedicamentoUseCase.ejecutar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<MedicamentoResponse> actualizar(
            @PathVariable("id") Integer id,
            @Valid @RequestBody ActualizarMedicamentoRequest request
    ) {
        return ResponseEntity.ok(actualizarMedicamentoUseCase.ejecutar(id, request));
    }

    @PatchMapping("/{id}/desactivar")
    @PreAuthorize("hasRole('ADMINISTRADORA')")
    public ResponseEntity<MedicamentoResponse> desactivar(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(desactivarMedicamentoUseCase.ejecutar(id));
    }
}
