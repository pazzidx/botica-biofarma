package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.MedicamentoResponse;
import com.biofarma.inventario.application.mapper.MedicamentoMapper;
import com.biofarma.inventario.domain.model.Medicamento;
import com.biofarma.inventario.domain.repository.MedicamentoRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para listar los medicamentos del catálogo (HU-04).
 */
@Service
public class ListarMedicamentosUseCase {

    private final MedicamentoRepository medicamentoRepository;
    private final MedicamentoMapper medicamentoMapper;

    public ListarMedicamentosUseCase(
            MedicamentoRepository medicamentoRepository,
            MedicamentoMapper medicamentoMapper
    ) {
        this.medicamentoRepository = medicamentoRepository;
        this.medicamentoMapper = medicamentoMapper;
    }

    @Transactional(readOnly = true)
    public List<MedicamentoResponse> ejecutar(Boolean soloActivos) {
        List<Medicamento> medicamentos = Boolean.TRUE.equals(soloActivos)
                ? medicamentoRepository.findByActivo(true)
                : medicamentoRepository.findAll();

        return medicamentos.stream()
                .map(medicamentoMapper::toResponse)
                .toList();
    }
}
