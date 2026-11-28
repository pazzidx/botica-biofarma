package com.biofarma.inventario.domain.repository;

import java.util.Collection;
import java.util.Map;

/**
 * Puerto de consulta de existencias vigentes en inventario (HU-05 / integración HU-06).
 * Desacopla la consulta de stock de la entidad de catálogo Medicamento.
 */
public interface StockInventarioRepository {

    /**
     * Obtiene el stock vigente consolidado para cada identificador de producto solicitado.
     *
     * @param idsProductos colección de identificadores de medicamentos activos
     * @return mapa {@code idProducto -> stockActual}
     */
    Map<Integer, Integer> obtenerStockActualPorProductos(Collection<Integer> idsProductos);
}
