package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.MedicamentoDisponibilidadResponse;
import com.biofarma.inventario.application.mapper.DisponibilidadMedicamentoMapper;
import com.biofarma.inventario.domain.exception.DatosInventarioInconsistentesException;
import com.biofarma.inventario.domain.exception.DatosMedicamentoInvalidosException;
import com.biofarma.inventario.domain.model.DisponibilidadMedicamento;
import com.biofarma.inventario.domain.model.Medicamento;
import com.biofarma.inventario.domain.repository.MedicamentoRepository;
import com.biofarma.inventario.domain.repository.StockInventarioRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso HU-05: Búsqueda rápida de medicamentos activos por código o nombre
 * y consulta de disponibilidad y stock vigente en tiempo real.
 */
@Service
public class ConsultarDisponibilidadMedicamentosUseCase {

    private static final int MAX_TERMINO_LENGTH = 150;
    private static final int LIMITE_POR_DEFECTO = 50;
    private static final int LIMITE_MAXIMO = 100;

    private final MedicamentoRepository medicamentoRepository;
    private final StockInventarioRepository stockInventarioRepository;
    private final DisponibilidadMedicamentoMapper disponibilidadMapper;

    public ConsultarDisponibilidadMedicamentosUseCase(
            MedicamentoRepository medicamentoRepository,
            StockInventarioRepository stockInventarioRepository,
            DisponibilidadMedicamentoMapper disponibilidadMapper
    ) {
        this.medicamentoRepository = medicamentoRepository;
        this.stockInventarioRepository = stockInventarioRepository;
        this.disponibilidadMapper = disponibilidadMapper;
    }

    @Transactional(readOnly = true)
    public List<MedicamentoDisponibilidadResponse> ejecutar(String termino, Integer limite) {
        String terminoNormalizado = validarYNormalizarTermino(termino);
        int limiteEfectivo = normalizarLimite(limite);

        List<Medicamento> coincidencias = medicamentoRepository.buscarActivosPorTermino(
                terminoNormalizado,
                limiteEfectivo
        );
        if (coincidencias == null || coincidencias.isEmpty()) {
            return List.of();
        }

        Map<Integer, Medicamento> medicamentosUnicos = deduplicarManteniendoOrden(coincidencias);
        Map<Integer, Integer> stockPorProducto = stockInventarioRepository.obtenerStockActualPorProductos(
                List.copyOf(medicamentosUnicos.keySet())
        );
        if (stockPorProducto == null) {
            throw new DatosInventarioInconsistentesException(
                    "No se pudo obtener el stock actualizado del inventario."
            );
        }

        return medicamentosUnicos.values().stream()
                .map(medicamento -> DisponibilidadMedicamento.evaluar(
                        medicamento,
                        stockPorProducto.get(medicamento.getIdProducto())
                ))
                .map(disponibilidadMapper::toResponse)
                .toList();
    }

    private String validarYNormalizarTermino(String termino) {
        if (termino == null || termino.isBlank()) {
            throw new DatosMedicamentoInvalidosException(
                    "Debe ingresar un nombre o código de medicamento para buscar."
            );
        }
        String normalizado = termino.trim();
        if (normalizado.length() > MAX_TERMINO_LENGTH) {
            throw new DatosMedicamentoInvalidosException(
                    "El término de búsqueda no puede superar los " + MAX_TERMINO_LENGTH + " caracteres."
            );
        }
        return normalizado;
    }

    private int normalizarLimite(Integer limite) {
        if (limite == null) {
            return LIMITE_POR_DEFECTO;
        }
        if (limite < 1 || limite > LIMITE_MAXIMO) {
            throw new DatosMedicamentoInvalidosException(
                    "El límite de resultados debe estar entre 1 y " + LIMITE_MAXIMO + "."
            );
        }
        return limite;
    }

    private Map<Integer, Medicamento> deduplicarManteniendoOrden(List<Medicamento> medicamentos) {
        Map<Integer, Medicamento> unicos = new LinkedHashMap<>();
        for (Medicamento medicamento : medicamentos) {
            if (medicamento != null && medicamento.getIdProducto() != null) {
                unicos.putIfAbsent(medicamento.getIdProducto(), medicamento);
            }
        }
        return unicos;
    }
}
