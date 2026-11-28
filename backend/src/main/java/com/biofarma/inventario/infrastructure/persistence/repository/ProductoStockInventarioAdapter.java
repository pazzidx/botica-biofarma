package com.biofarma.inventario.infrastructure.persistence.repository;

import com.biofarma.inventario.domain.repository.StockInventarioRepository;
import com.biofarma.inventario.infrastructure.persistence.repository.SpringDataProductoRepository.ProductoStockProjection;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de consulta de stock vigente en inventario (HU-05).
 * Lee de forma consolidada en una única consulta SQL evitando N+1.
 *
 * <p>Punto de extensión para HU-06: cuando el módulo de lotes/ingresos calcule existencias
 * directamente desde Lotes o actualice Productos.stock_actual transaccionalmente,
 * este puerto garantiza que HU-05 consuma siempre el inventario actualizado sin cambios en capas superiores.</p>
 */
@Repository
public class ProductoStockInventarioAdapter implements StockInventarioRepository {

    private final SpringDataProductoRepository springDataProductoRepository;

    public ProductoStockInventarioAdapter(SpringDataProductoRepository springDataProductoRepository) {
        this.springDataProductoRepository = springDataProductoRepository;
    }

    @Override
    public Map<Integer, Integer> obtenerStockActualPorProductos(Collection<Integer> idsProductos) {
        if (idsProductos == null || idsProductos.isEmpty()) {
            return Map.of();
        }
        List<ProductoStockProjection> proyecciones = springDataProductoRepository.obtenerStockPorIds(idsProductos);
        Map<Integer, Integer> stockPorProducto = new HashMap<>(proyecciones.size());
        for (ProductoStockProjection proyeccion : proyecciones) {
            stockPorProducto.put(proyeccion.getIdProducto(), proyeccion.getStockActual());
        }
        return stockPorProducto;
    }
}
