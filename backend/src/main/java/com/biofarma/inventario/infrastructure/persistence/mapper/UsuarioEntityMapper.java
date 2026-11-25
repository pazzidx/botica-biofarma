package com.biofarma.inventario.infrastructure.persistence.mapper;

import com.biofarma.inventario.domain.model.Rol;
import com.biofarma.inventario.domain.model.RolNombre;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.infrastructure.persistence.entity.RolEntity;
import com.biofarma.inventario.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper entre entidades JPA y modelos puros de dominio.
 */
@Component
public class UsuarioEntityMapper {

    public Rol toDomain(RolEntity entity) {
        return new Rol(
                entity.getIdRol(),
                RolNombre.fromString(entity.getNombre())
        );
    }

    public RolEntity toEntity(Rol domain) {
        return new RolEntity(
                domain.getIdRol(),
                domain.getNombre().name()
        );
    }

    public Usuario toDomain(UsuarioEntity entity) {
        return Usuario.reconstruir(
                entity.getIdUsuario(),
                entity.getCodigoUsuario(),
                entity.getNombres(),
                entity.getApellidos(),
                entity.getPasswordHash(),
                toDomain(entity.getRol()),
                entity.isActivo(),
                entity.getFechaCreacion()
        );
    }

    public UsuarioEntity toEntity(Usuario domain, RolEntity rolEntity) {
        UsuarioEntity entity = new UsuarioEntity();
        entity.setIdUsuario(domain.getIdUsuario());
        entity.setCodigoUsuario(domain.getCodigoUsuario());
        entity.setNombres(domain.getNombres());
        entity.setApellidos(domain.getApellidos());
        entity.setPasswordHash(domain.getPasswordHash());
        entity.setRol(rolEntity);
        entity.setActivo(domain.isActivo());
        entity.setFechaCreacion(domain.getFechaCreacion());
        return entity;
    }
}
