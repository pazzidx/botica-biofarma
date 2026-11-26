package com.biofarma.inventario.infrastructure.persistence.mapper;

import com.biofarma.inventario.domain.model.Medicamento;
import com.biofarma.inventario.infrastructure.persistence.entity.ProductoEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre la entidad JPA ProductoEntity y la entidad de dominio Medicamento.
 */
@Component
public class ProductoEntityMapper {

    public Medicamento toDomain(ProductoEntity entity) {
        return Medicamento.reconstruir(
                entity.getIdProducto(),
                entity.getCodigoProducto(),
                entity.getNombreComercial(),
                entity.getPrincipioActivo(),
                entity.getPresentacion(),
                entity.getLaboratorio(),
                entity.getPrecioVenta(),
                entity.getStockMinimo(),
                entity.isRequiereReceta(),
                entity.isActivo(),
                entity.getFechaRegistro()
        );
    }

    public ProductoEntity toEntity(Medicamento medicamento) {
        ProductoEntity entity = new ProductoEntity();
        entity.setIdProducto(medicamento.getIdProducto());
        entity.setCodigoProducto(medicamento.getCodigoProducto());
        entity.setNombreComercial(medicamento.getNombreComercial());
        entity.setPrincipioActivo(medicamento.getPrincipioActivo());
        entity.setPresentacion(medicamento.getPresentacion());
        entity.setLaboratorio(medicamento.getLaboratorio());
        entity.setPrecioVenta(medicamento.getPrecioVenta());
        entity.setStockMinimo(medicamento.getStockMinimo());
        entity.setRequiereReceta(medicamento.isRequiereReceta());
        entity.setActivo(medicamento.isActivo());
        entity.setFechaRegistro(medicamento.getFechaRegistro());
        return entity;
    }
}
