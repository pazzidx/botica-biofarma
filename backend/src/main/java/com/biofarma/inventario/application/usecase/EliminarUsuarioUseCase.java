package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.EliminacionUsuarioResponse;
import com.biofarma.inventario.application.mapper.UsuarioMapper;
import com.biofarma.inventario.domain.exception.OperacionNoPermitidaException;
import com.biofarma.inventario.domain.exception.UsuarioNoEncontradoException;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de eliminación inteligente de usuarios (HU-01):
 * borrado físico si no tiene ventas asociadas; desactivación lógica si tiene ventas registradas.
 */
@Service
public class EliminarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public EliminarUsuarioUseCase(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional
    public EliminacionUsuarioResponse ejecutar(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException(idUsuario));

        if (usuario.esProtegido() || esSesionActual(usuario)) {
            throw new OperacionNoPermitidaException(
                    "No está permitido eliminar o desactivar la cuenta administradora ni la sesión actual."
            );
        }

        long ventasAsociadas = usuarioRepository.countVentasByUsuarioId(idUsuario);
        if (ventasAsociadas == 0) {
            usuarioRepository.deleteById(idUsuario);
            return new EliminacionUsuarioResponse(
                    idUsuario,
                    true,
                    "Usuario '" + usuario.getCodigoUsuario() + "' eliminado definitivamente (0 ventas asociadas).",
                    null
            );
        }

        Usuario desactivado = usuarioRepository.save(usuario.desactivar());
        return new EliminacionUsuarioResponse(
                idUsuario,
                false,
                "Usuario '" + usuario.getCodigoUsuario() + "' pasó a estado Inactivo porque registra "
                        + ventasAsociadas + " venta(s).",
                usuarioMapper.toResponse(desactivado)
        );
    }

    private boolean esSesionActual(Usuario usuario) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String principal = authentication.getName();
        return principal != null && principal.equalsIgnoreCase(usuario.getCodigoUsuario());
    }
}
