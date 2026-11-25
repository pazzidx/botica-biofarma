package com.biofarma.inventario.infrastructure.persistence.repository;

import com.biofarma.inventario.domain.exception.RolNoEncontradoException;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import com.biofarma.inventario.infrastructure.persistence.entity.RolEntity;
import com.biofarma.inventario.infrastructure.persistence.entity.UsuarioEntity;
import com.biofarma.inventario.infrastructure.persistence.mapper.UsuarioEntityMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Adaptador de persistencia para el puerto {@link UsuarioRepository}.
 */
@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final SpringDataUsuarioRepository jpaUsuarioRepository;
    private final SpringDataRolRepository jpaRolRepository;
    private final UsuarioEntityMapper mapper;

    public UsuarioRepositoryAdapter(
            SpringDataUsuarioRepository jpaUsuarioRepository,
            SpringDataRolRepository jpaRolRepository,
            UsuarioEntityMapper mapper
    ) {
        this.jpaUsuarioRepository = jpaUsuarioRepository;
        this.jpaRolRepository = jpaRolRepository;
        this.mapper = mapper;
    }

    @Override
    public boolean existsByCodigoUsuario(String codigoUsuario) {
        return jpaUsuarioRepository.existsByCodigoUsuarioIgnoreCase(codigoUsuario);
    }

    @Override
    public Usuario save(Usuario usuario) {
        String nombreRol = usuario.getRol().getNombre().name();
        RolEntity rolEntity = jpaRolRepository.findByNombre(nombreRol)
                .orElseThrow(() -> new RolNoEncontradoException(nombreRol));

        UsuarioEntity entity = mapper.toEntity(usuario, rolEntity);
        UsuarioEntity saved = jpaUsuarioRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Usuario> findById(Integer idUsuario) {
        return jpaUsuarioRepository.findById(idUsuario)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Usuario> findByCodigoUsuario(String codigoUsuario) {
        return jpaUsuarioRepository.findByCodigoUsuarioIgnoreCase(codigoUsuario)
                .map(mapper::toDomain);
    }

    @Override
    public List<Usuario> findAll() {
        return jpaUsuarioRepository.findAllByOrderByFechaCreacionDesc().stream()
                .map(mapper::toDomain)
                .toList();
    }
}
