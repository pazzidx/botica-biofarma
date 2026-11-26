package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.MedicamentoResponse;
import com.biofarma.inventario.application.mapper.MedicamentoMapper;
import com.biofarma.inventario.domain.exception.MedicamentoNoEncontradoException;
import com.biofarma.inventario.domain.model.Medicamento;
import com.biofarma.inventario.domain.repository.MedicamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para desactivar lógicamente un medicamento del catálogo sin perder historial (HU-04).
 */
@Service
public class DesactivarMedicamentoUseCase {

    private final MedicamentoRepository medicamentoRepository;
    private final MedicamentoMapper medicamentoMapper;

    public DesactivarMedicamentoUseCase(
            MedicamentoRepository medicamentoRepository,
            MedicamentoMapper medicamentoMapper
    ) {
        this.medicamentoRepository = medicamentoRepository;
        this.medicamentoMapper = medicamentoMapper;
    }

    @Transactional
    public MedicamentoResponse ejecutar(Integer idProducto) {
        Medicamento existente = medicamentoRepository.findById(idProducto)
                .orElseThrow(() -> new MedicamentoNoEncontradoException(idProducto));

        Medicamento desactivado = existente.desactivar();
        Medicamento guardado = medicamentoRepository.save(desactivado);
        return medicamentoMapper.toResponse(guardado);
    }
}
