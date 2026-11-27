package com.biofarma.inventario.infrastructure.persistence.repository;

import com.biofarma.inventario.domain.model.Venta;
import com.biofarma.inventario.domain.repository.VentaRepository;
import com.biofarma.inventario.infrastructure.persistence.mapper.VentaEntityMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA que implementa el puerto VentaRepository del dominio.
 */
@Repository
public class VentaRepositoryAdapter implements VentaRepository {

    private final SpringDataVentaRepository springDataVentaRepository;
    private final VentaEntityMapper ventaEntityMapper;

    public VentaRepositoryAdapter(
            SpringDataVentaRepository springDataVentaRepository,
            VentaEntityMapper ventaEntityMapper
    ) {
        this.springDataVentaRepository = springDataVentaRepository;
        this.ventaEntityMapper = ventaEntityMapper;
    }

    @Override
    public List<Venta> listarTodasOrdenadasPorFechaDesc() {
        return springDataVentaRepository.findAllWithUsuarioAndDetallesOrderByFechaVentaDesc()
                .stream()
                .map(ventaEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Venta> listarPorUsuarioOrdenadasPorFechaDesc(Integer idUsuario) {
        return springDataVentaRepository.findByUsuarioIdWithDetallesOrderByFechaVentaDesc(idUsuario)
                .stream()
                .map(ventaEntityMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Venta> buscarPorIdConDetalle(Integer idVenta) {
        return springDataVentaRepository.findByIdWithUsuarioAndDetalles(idVenta)
                .map(ventaEntityMapper::toDomain);
    }
}
