package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.UsuarioResponse;
import com.biofarma.inventario.application.mapper.UsuarioMapper;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para consultar el listado de usuarios registrados (HU-01).
 */
@Service
public class ListarUsuariosUseCase {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public ListarUsuariosUseCase(UsuarioRepository usuarioRepository, UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> ejecutar() {
        return usuarioMapper.toResponseList(usuarioRepository.findAll());
    }
}
