package com.biofarma.inventario.application.mapper;

import com.biofarma.inventario.application.dto.UsuarioResponse;
import com.biofarma.inventario.domain.model.Usuario;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Mapper entre la entidad de dominio {@link Usuario} y sus DTOs de aplicación.
 */
@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getIdUsuario(),
                usuario.getCodigoUsuario(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getRol().getNombre().name(),
                usuario.isActivo(),
                usuario.getFechaCreacion()
        );
    }

    public List<UsuarioResponse> toResponseList(List<Usuario> usuarios) {
        return usuarios.stream()
                .map(this::toResponse)
                .toList();
    }
}
