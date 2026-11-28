package com.biofarma.inventario.application.mapper;

import com.biofarma.inventario.application.dto.MedicamentoDisponibilidadResponse;
import com.biofarma.inventario.domain.model.DisponibilidadMedicamento;
import org.springframework.stereotype.Component;

/**
 * Mapper entre el modelo de dominio DisponibilidadMedicamento y su DTO de respuesta REST.
 */
@Component
public class DisponibilidadMedicamentoMapper {

    public MedicamentoDisponibilidadResponse toResponse(DisponibilidadMedicamento disponibilidad) {
        return new MedicamentoDisponibilidadResponse(
                disponibilidad.idProducto(),
                disponibilidad.codigoProducto(),
                disponibilidad.nombreComercial(),
                disponibilidad.principioActivo(),
                disponibilidad.presentacion(),
                disponibilidad.laboratorio(),
                disponibilidad.precioVenta(),
                disponibilidad.stockActual(),
                disponibilidad.requiereReceta(),
                disponibilidad.estadoDisponibilidad().etiqueta(),
                disponibilidad.disponible()
        );
    }
}
