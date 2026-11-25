package com.biofarma.inventario.infrastructure.persistence.repository;

import com.biofarma.inventario.domain.model.Rol;
import com.biofarma.inventario.domain.model.RolNombre;
import com.biofarma.inventario.domain.repository.RolRepository;
import com.biofarma.inventario.infrastructure.persistence.mapper.UsuarioEntityMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de persistencia para el puerto {@link RolRepository}.
 */
@Repository
public class RolRepositoryAdapter implements RolRepository {

    private final SpringDataRolRepository jpaRepository;
    private final UsuarioEntityMapper mapper;

    public RolRepositoryAdapter(SpringDataRolRepository jpaRepository, UsuarioEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Rol> findByNombre(RolNombre nombre) {
        return jpaRepository.findByNombre(nombre.name())
                .map(mapper::toDomain);
    }

    @Override
    public List<Rol> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
