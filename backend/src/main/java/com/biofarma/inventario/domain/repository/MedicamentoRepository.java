package com.biofarma.inventario.domain.repository;

import com.biofarma.inventario.domain.model.Medicamento;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia de medicamentos del catálogo.
 */
public interface MedicamentoRepository {

    boolean existsByCodigoProducto(String codigoProducto);

    Medicamento save(Medicamento medicamento);

    Optional<Medicamento> findById(Integer idProducto);

    List<Medicamento> findAll();

    List<Medicamento> findByActivo(boolean activo);
}
