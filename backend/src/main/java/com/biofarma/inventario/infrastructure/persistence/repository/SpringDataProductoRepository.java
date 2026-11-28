package com.biofarma.inventario.infrastructure.persistence.repository;

import com.biofarma.inventario.infrastructure.persistence.entity.ProductoEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio Spring Data JPA para la tabla Productos.
 */
public interface SpringDataProductoRepository extends JpaRepository<ProductoEntity, Integer> {

    boolean existsByCodigoProducto(String codigoProducto);

    List<ProductoEntity> findByActivoOrderByNombreComercialAsc(boolean activo);

    List<ProductoEntity> findAllByOrderByNombreComercialAsc();

    @Query("""
            SELECT p
            FROM ProductoEntity p
            WHERE p.activo = true
              AND (
                    LOWER(p.codigoProducto) LIKE :patron ESCAPE '!'
                    OR LOWER(p.nombreComercial) LIKE :patron ESCAPE '!'
                    OR LOWER(p.principioActivo) LIKE :patron ESCAPE '!'
              )
            ORDER BY
              CASE
                WHEN LOWER(p.codigoProducto) = :terminoExacto THEN 0
                WHEN LOWER(p.codigoProducto) LIKE :patronPrefijo ESCAPE '!' THEN 1
                WHEN LOWER(p.nombreComercial) LIKE :patronPrefijo ESCAPE '!' THEN 2
                ELSE 3
              END,
              p.nombreComercial ASC
            """)
    List<ProductoEntity> buscarActivosPorTermino(
            @Param("terminoExacto") String terminoExacto,
            @Param("patron") String patron,
            @Param("patronPrefijo") String patronPrefijo,
            Pageable pageable
    );

    @Query("""
            SELECT p.idProducto AS idProducto, p.stockActual AS stockActual
            FROM ProductoEntity p
            WHERE p.idProducto IN :idsProductos
            """)
    List<ProductoStockProjection> obtenerStockPorIds(@Param("idsProductos") Collection<Integer> idsProductos);

    interface ProductoStockProjection {
        Integer getIdProducto();

        Integer getStockActual();
    }
}
