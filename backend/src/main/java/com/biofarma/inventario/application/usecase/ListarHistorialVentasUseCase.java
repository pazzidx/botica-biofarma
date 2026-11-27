package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.VentaResumenResponse;
import com.biofarma.inventario.application.mapper.VentaMapper;
import com.biofarma.inventario.domain.model.AuthenticatedUser;
import com.biofarma.inventario.domain.model.Venta;
import com.biofarma.inventario.domain.repository.VentaRepository;
import com.biofarma.inventario.domain.service.AuthenticatedUserProvider;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso HU-03 (ST-03.1): Lista el historial de ventas filtrando por usuario autenticado
 * cuando el rol es TRABAJADORA, o devolviendo todo el historial cuando el rol es ADMINISTRADORA.
 */
@Service
public class ListarHistorialVentasUseCase {

    private final VentaRepository ventaRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final VentaMapper ventaMapper;

    public ListarHistorialVentasUseCase(
            VentaRepository ventaRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            VentaMapper ventaMapper
    ) {
        this.ventaRepository = ventaRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.ventaMapper = ventaMapper;
    }

    @Transactional(readOnly = true)
    public List<VentaResumenResponse> ejecutar() {
        AuthenticatedUser usuarioActual = authenticatedUserProvider.obtenerUsuarioAutenticado();

        List<Venta> ventas = usuarioActual.esAdministradora()
                ? ventaRepository.listarTodasOrdenadasPorFechaDesc()
                : ventaRepository.listarPorUsuarioOrdenadasPorFechaDesc(usuarioActual.idUsuario());

        return ventas.stream()
                .map(ventaMapper::toResumenResponse)
                .toList();
    }
}
