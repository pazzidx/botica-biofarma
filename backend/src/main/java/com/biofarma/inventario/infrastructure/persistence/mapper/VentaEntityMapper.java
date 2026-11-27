package com.biofarma.inventario.infrastructure.persistence.mapper;

import com.biofarma.inventario.domain.model.DetalleVenta;
import com.biofarma.inventario.domain.model.Venta;
import com.biofarma.inventario.infrastructure.persistence.entity.DetalleVentaEntity;
import com.biofarma.inventario.infrastructure.persistence.entity.UsuarioEntity;
import com.biofarma.inventario.infrastructure.persistence.entity.VentaEntity;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Mapea entidades JPA de ventas a modelos puros de dominio.
 */
@Component
public class VentaEntityMapper {

    public Venta toDomain(VentaEntity entity) {
        UsuarioEntity usuario = entity.getUsuario();
        String nombreUsuario = usuario.getNombres() + " " + usuario.getApellidos();

        List<DetalleVenta> detalles = entity.getDetalles() == null
                ? List.of()
                : entity.getDetalles().stream().map(this::toDetalleDomain).toList();

        return new Venta(
                entity.getIdVenta(),
                entity.getNumeroTicket(),
                usuario.getIdUsuario(),
                usuario.getCodigoUsuario(),
                nombreUsuario.trim(),
                entity.getFechaVenta(),
                entity.getTotal(),
                entity.getEstado(),
                detalles
        );
    }

    private DetalleVenta toDetalleDomain(DetalleVentaEntity entity) {
        return new DetalleVenta(
                entity.getIdDetalle(),
                entity.getIdLote(),
                entity.getNombreProducto(),
                entity.getNumeroLote(),
                entity.getCantidad(),
                entity.getPrecioUnitario(),
                entity.getSubtotal()
        );
    }
}
