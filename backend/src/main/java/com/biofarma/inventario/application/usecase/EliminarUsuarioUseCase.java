package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Caso de uso para eliminar o desactivar cuentas de usuario (solo ADMINISTRADORA).
 */
@Service
public class EliminarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;

    public EliminarUsuarioUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void ejecutar(Integer idUsuario, String codigoUsuarioAutenticado) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El usuario solicitado no existe."
                ));

        if (codigoUsuarioAutenticado != null
                && usuario.getCodigoUsuario().equalsIgnoreCase(codigoUsuarioAutenticado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "No puede eliminar su propia cuenta de administradora."
            );
        }

        try {
            usuarioRepository.deleteById(idUsuario);
        } catch (DataIntegrityViolationException ex) {
            // Si el usuario tiene ventas asociadas en la BD, se desactiva para mantener integridad referencial
            Usuario usuarioDesactivado = Usuario.reconstruir(
                    usuario.getIdUsuario(),
                    usuario.getCodigoUsuario(),
                    usuario.getNombres(),
                    usuario.getApellidos(),
                    usuario.getPasswordHash(),
                    usuario.getRol(),
                    false,
                    usuario.getFechaCreacion()
            );
            usuarioRepository.save(usuarioDesactivado);
        }
    }
}