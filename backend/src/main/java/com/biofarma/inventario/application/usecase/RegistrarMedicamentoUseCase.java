package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.MedicamentoResponse;
import com.biofarma.inventario.application.dto.RegistrarMedicamentoRequest;
import com.biofarma.inventario.application.mapper.MedicamentoMapper;
import com.biofarma.inventario.domain.exception.CodigoMedicamentoDuplicadoException;
import com.biofarma.inventario.domain.model.Medicamento;
import com.biofarma.inventario.domain.repository.MedicamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para registrar nuevos medicamentos en el catálogo (HU-04).
 */
@Service
public class RegistrarMedicamentoUseCase {

    private final MedicamentoRepository medicamentoRepository;
    private final MedicamentoMapper medicamentoMapper;

    public RegistrarMedicamentoUseCase(
            MedicamentoRepository medicamentoRepository,
            MedicamentoMapper medicamentoMapper
    ) {
        this.medicamentoRepository = medicamentoRepository;
        this.medicamentoMapper = medicamentoMapper;
    }

    @Transactional
    public MedicamentoResponse ejecutar(RegistrarMedicamentoRequest request) {
        String codigoNormalizado = request.codigoProducto() == null ? "" : request.codigoProducto().trim();
        if (!codigoNormalizado.isEmpty() && medicamentoRepository.existsByCodigoProducto(codigoNormalizado)) {
            throw new CodigoMedicamentoDuplicadoException(codigoNormalizado);
        }

        Medicamento nuevo = Medicamento.crearNuevo(
                request.codigoProducto(),
                request.nombreComercial(),
                request.principioActivo(),
                request.presentacion(),
                request.laboratorio(),
                request.precioVenta(),
                request.stockMinimo() == null ? -1 : request.stockMinimo(),
                Boolean.TRUE.equals(request.requiereReceta())
        );

        Medicamento guardado = medicamentoRepository.save(nuevo);
        return medicamentoMapper.toResponse(guardado);
    }
}
