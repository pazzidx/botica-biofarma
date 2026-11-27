package com.biofarma.inventario.presentation.controller;

import com.biofarma.inventario.application.dto.VentaDetalleResponse;
import com.biofarma.inventario.application.dto.VentaResumenResponse;
import com.biofarma.inventario.application.usecase.ListarHistorialVentasUseCase;
import com.biofarma.inventario.application.usecase.ObtenerDetalleVentaUseCase;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST delgado para consulta de historial y detalle de ventas (HU-03).
 * La identidad y rol se obtienen siempre del token JWT en el servidor, ignorando parámetros del cliente.
 */
@RestController
@RequestMapping("/api/v1/ventas")
public class VentaController {

    private final ListarHistorialVentasUseCase listarHistorialVentasUseCase;
    private final ObtenerDetalleVentaUseCase obtenerDetalleVentaUseCase;

    public VentaController(
            ListarHistorialVentasUseCase listarHistorialVentasUseCase,
            ObtenerDetalleVentaUseCase obtenerDetalleVentaUseCase
    ) {
        this.listarHistorialVentasUseCase = listarHistorialVentasUseCase;
        this.obtenerDetalleVentaUseCase = obtenerDetalleVentaUseCase;
    }

    @GetMapping("/historial")
    @PreAuthorize("hasAnyRole('ADMINISTRADORA', 'TRABAJADORA')")
    public ResponseEntity<List<VentaResumenResponse>> listarHistorial() {
        return ResponseEntity.ok(listarHistorialVentasUseCase.ejecutar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADORA', 'TRABAJADORA')")
    public ResponseEntity<VentaDetalleResponse> obtenerDetalle(@PathVariable Integer id) {
        return ResponseEntity.ok(obtenerDetalleVentaUseCase.ejecutar(id));
    }
}
