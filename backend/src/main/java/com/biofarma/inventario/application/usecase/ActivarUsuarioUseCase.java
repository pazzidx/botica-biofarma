package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.UsuarioResponse;
import com.biofarma.inventario.application.mapper.UsuarioMapper;
import com.biofarma.inventario.domain.exception.UsuarioNoEncontradoException;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para reactivar una cuenta inactiva (HU-01).
 */
@Service
public class ActivarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public ActivarUsuarioUseCase(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional
    public UsuarioResponse ejecutar(Integer idUsuario) {
        Usuario existente = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException(idUsuario));

        Usuario activado = usuarioRepository.save(existente.activar());
        return usuarioMapper.toResponse(activado);
    }
}
