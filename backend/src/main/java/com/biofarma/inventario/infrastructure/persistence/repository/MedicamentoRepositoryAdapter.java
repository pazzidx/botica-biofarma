package com.biofarma.inventario.infrastructure.persistence.repository;

import com.biofarma.inventario.domain.model.Medicamento;
import com.biofarma.inventario.domain.repository.MedicamentoRepository;
import com.biofarma.inventario.infrastructure.persistence.entity.ProductoEntity;
import com.biofarma.inventario.infrastructure.persistence.mapper.ProductoEntityMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de persistencia que implementa MedicamentoRepository usando Spring Data JPA.
 */
@Repository
public class MedicamentoRepositoryAdapter implements MedicamentoRepository {

    private final SpringDataProductoRepository springDataRepository;
    private final ProductoEntityMapper mapper;

    public MedicamentoRepositoryAdapter(
            SpringDataProductoRepository springDataRepository,
            ProductoEntityMapper mapper
    ) {
        this.springDataRepository = springDataRepository;
        this.mapper = mapper;
    }

    @Override
    public boolean existsByCodigoProducto(String codigoProducto) {
        return springDataRepository.existsByCodigoProducto(codigoProducto);
    }

    @Override
    public Medicamento save(Medicamento medicamento) {
        ProductoEntity entity = mapper.toEntity(medicamento);
        if (medicamento.getIdProducto() != null) {
            int stockActual = springDataRepository.findById(medicamento.getIdProducto())
                    .map(ProductoEntity::getStockActual)
                    .orElse(0);
            entity.setStockActual(stockActual);
        }
        return mapper.toDomain(springDataRepository.save(entity));
    }

    @Override
    public Optional<Medicamento> findById(Integer idProducto) {
        return springDataRepository.findById(idProducto).map(mapper::toDomain);
    }

    @Override
    public List<Medicamento> findAll() {
        return springDataRepository.findAllByOrderByNombreComercialAsc()
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Medicamento> findByActivo(boolean activo) {
        return springDataRepository.findByActivoOrderByNombreComercialAsc(activo)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
