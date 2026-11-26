package com.biofarma.inventario.infrastructure.persistence.repository;

import com.biofarma.inventario.infrastructure.persistence.entity.ProductoEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA para la tabla Productos.
 */
public interface SpringDataProductoRepository extends JpaRepository<ProductoEntity, Integer> {

    boolean existsByCodigoProducto(String codigoProducto);

    List<ProductoEntity> findByActivoOrderByNombreComercialAsc(boolean activo);

    List<ProductoEntity> findAllByOrderByNombreComercialAsc();
}
