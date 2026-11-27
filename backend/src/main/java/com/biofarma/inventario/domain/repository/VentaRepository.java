package com.biofarma.inventario.domain.repository;

import com.biofarma.inventario.domain.model.Venta;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para consultar ventas y su detalle en persistencia.
 */
public interface VentaRepository {

    List<Venta> listarTodasOrdenadasPorFechaDesc();

    List<Venta> listarPorUsuarioOrdenadasPorFechaDesc(Integer idUsuario);

    Optional<Venta> buscarPorIdConDetalle(Integer idVenta);
}
