package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.ActualizarMedicamentoRequest;
import com.biofarma.inventario.application.dto.MedicamentoResponse;
import com.biofarma.inventario.application.mapper.MedicamentoMapper;
import com.biofarma.inventario.domain.exception.MedicamentoNoEncontradoException;
import com.biofarma.inventario.domain.model.Medicamento;
import com.biofarma.inventario.domain.repository.MedicamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para actualizar los datos comerciales de un medicamento existente (HU-04).
 */
@Service
public class ActualizarMedicamentoUseCase {

    private final MedicamentoRepository medicamentoRepository;
    private final MedicamentoMapper medicamentoMapper;

    public ActualizarMedicamentoUseCase(
            MedicamentoRepository medicamentoRepository,
            MedicamentoMapper medicamentoMapper
    ) {
        this.medicamentoRepository = medicamentoRepository;
        this.medicamentoMapper = medicamentoMapper;
    }

    @Transactional
    public MedicamentoResponse ejecutar(Integer idProducto, ActualizarMedicamentoRequest request) {
        Medicamento existente = medicamentoRepository.findById(idProducto)
                .orElseThrow(() -> new MedicamentoNoEncontradoException(idProducto));

        Medicamento actualizado = existente.actualizar(
                request.nombreComercial(),
                request.principioActivo(),
                request.presentacion(),
                request.laboratorio(),
                request.precioVenta(),
                request.stockMinimo() == null ? -1 : request.stockMinimo(),
                Boolean.TRUE.equals(request.requiereReceta())
        );

        Medicamento guardado = medicamentoRepository.save(actualizado);
        return medicamentoMapper.toResponse(guardado);
    }
}
