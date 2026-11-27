package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.VentaDetalleResponse;
import com.biofarma.inventario.application.mapper.VentaMapper;
import com.biofarma.inventario.domain.exception.VentaNoEncontradaException;
import com.biofarma.inventario.domain.model.AuthenticatedUser;
import com.biofarma.inventario.domain.model.Venta;
import com.biofarma.inventario.domain.repository.VentaRepository;
import com.biofarma.inventario.domain.service.AuthenticatedUserProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso HU-03 (ST-03.2): Consulta el detalle de una venta individual validando
 * la propiedad del registro cuando el rol es TRABAJADORA.
 */
@Service
public class ObtenerDetalleVentaUseCase {

    private final VentaRepository ventaRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final VentaMapper ventaMapper;

    public ObtenerDetalleVentaUseCase(
            VentaRepository ventaRepository,
            AuthenticatedUserProvider authenticatedUserProvider,
            VentaMapper ventaMapper
    ) {
        this.ventaRepository = ventaRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.ventaMapper = ventaMapper;
    }

    @Transactional(readOnly = true)
    public VentaDetalleResponse ejecutar(Integer idVenta) {
        AuthenticatedUser usuarioActual = authenticatedUserProvider.obtenerUsuarioAutenticado();

        Venta venta = ventaRepository.buscarPorIdConDetalle(idVenta)
                .orElseThrow(() -> new VentaNoEncontradaException(idVenta));

        venta.validarAcceso(usuarioActual);

        return ventaMapper.toDetalleResponse(venta);
    }
}
