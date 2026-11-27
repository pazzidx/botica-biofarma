package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.UsuarioResponse;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Caso de uso para activar o desactivar cuentas de usuario (solo ADMINISTRADORA).
 */
@Service
public class CambiarEstadoUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    public CambiarEstadoUsuarioUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponse ejecutar(Integer idUsuario, String codigoUsuarioAutenticado) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El usuario solicitado no existe."
                ));

        if (codigoUsuarioAutenticado != null
                && usuario.getCodigoUsuario().equalsIgnoreCase(codigoUsuarioAutenticado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No puede cambiar el estado de su propia cuenta."
            );
        }

        Usuario usuarioActualizado = Usuario.reconstruir(
                usuario.getIdUsuario(),
                usuario.getCodigoUsuario(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getPasswordHash(),
                usuario.getRol(),
                !usuario.isActivo(),
                usuario.getFechaCreacion()
        );

        Usuario guardado = usuarioRepository.save(usuarioActualizado);

        return new UsuarioResponse(
                guardado.getIdUsuario(),
                guardado.getCodigoUsuario(),
                guardado.getNombres(),
                guardado.getApellidos(),
                guardado.getRol().getNombre().name(),
                guardado.isActivo(),
                guardado.getFechaCreacion()
        );
    }
}