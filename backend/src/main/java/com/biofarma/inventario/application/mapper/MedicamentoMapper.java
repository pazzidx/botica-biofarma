package com.biofarma.inventario.application.mapper;

import com.biofarma.inventario.application.dto.MedicamentoResponse;
import com.biofarma.inventario.domain.model.Medicamento;
import org.springframework.stereotype.Component;

/**
 * Mapper entre la entidad de dominio Medicamento y su DTO de respuesta.
 */
@Component
public class MedicamentoMapper {

    public MedicamentoResponse toResponse(Medicamento medicamento) {
        return new MedicamentoResponse(
                medicamento.getIdProducto(),
                medicamento.getCodigoProducto(),
                medicamento.getNombreComercial(),
                medicamento.getPrincipioActivo(),
                medicamento.getPresentacion(),
                medicamento.getLaboratorio(),
                medicamento.getPrecioVenta(),
                medicamento.getStockMinimo(),
                medicamento.isRequiereReceta(),
                medicamento.isActivo(),
                medicamento.getFechaRegistro()
        );
    }
}
