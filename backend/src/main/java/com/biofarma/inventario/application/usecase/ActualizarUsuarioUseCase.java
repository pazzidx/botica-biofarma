package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.ActualizarUsuarioRequest;
import com.biofarma.inventario.application.dto.UsuarioResponse;
import com.biofarma.inventario.application.mapper.UsuarioMapper;
import com.biofarma.inventario.domain.exception.UsuarioNoEncontradoException;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para editar nombres y apellidos de una trabajadora (HU-01).
 */
@Service
public class ActualizarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public ActualizarUsuarioUseCase(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional
    public UsuarioResponse ejecutar(Integer idUsuario, ActualizarUsuarioRequest request) {
        Usuario existente = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException(idUsuario));

        Usuario actualizado = existente.actualizarDatosPersonales(request.nombres(), request.apellidos());
        Usuario guardado = usuarioRepository.save(actualizado);
        return usuarioMapper.toResponse(guardado);
    }
}
