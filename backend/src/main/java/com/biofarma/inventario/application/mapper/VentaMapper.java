package com.biofarma.inventario.application.mapper;

import com.biofarma.inventario.application.dto.DetalleVentaResponse;
import com.biofarma.inventario.application.dto.VentaDetalleResponse;
import com.biofarma.inventario.application.dto.VentaResumenResponse;
import com.biofarma.inventario.domain.model.DetalleVenta;
import com.biofarma.inventario.domain.model.Venta;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Conversor entre entidades de dominio de ventas y DTOs de salida.
 */
@Component
public class VentaMapper {

    public VentaResumenResponse toResumenResponse(Venta venta) {
        return new VentaResumenResponse(
                venta.getIdVenta(),
                venta.getNumeroTicket(),
                venta.getIdUsuario(),
                venta.getCodigoUsuario(),
                venta.getNombreUsuario(),
                venta.getFechaVenta(),
                venta.getTotal(),
                venta.getEstado(),
                venta.getCantidadItems()
        );
    }

    public VentaDetalleResponse toDetalleResponse(Venta venta) {
        List<DetalleVentaResponse> detalles = venta.getDetalles().stream()
                .map(this::toDetalleItemResponse)
                .toList();

        return new VentaDetalleResponse(
                venta.getIdVenta(),
                venta.getNumeroTicket(),
                venta.getIdUsuario(),
                venta.getCodigoUsuario(),
                venta.getNombreUsuario(),
                venta.getFechaVenta(),
                venta.getTotal(),
                venta.getEstado(),
                detalles
        );
    }

    private DetalleVentaResponse toDetalleItemResponse(DetalleVenta detalle) {
        return new DetalleVentaResponse(
                detalle.getIdDetalle(),
                detalle.getIdLote(),
                detalle.getNombreProducto(),
                detalle.getNumeroLote(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getSubtotal()
        );
    }
}
