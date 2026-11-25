package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.RegistrarUsuarioRequest;
import com.biofarma.inventario.application.dto.UsuarioResponse;
import com.biofarma.inventario.application.mapper.UsuarioMapper;
import com.biofarma.inventario.domain.exception.CodigoUsuarioDuplicadoException;
import com.biofarma.inventario.domain.exception.RolNoEncontradoException;
import com.biofarma.inventario.domain.model.Rol;
import com.biofarma.inventario.domain.model.RolNombre;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.RolRepository;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import com.biofarma.inventario.domain.service.PasswordHasher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para el registro seguro de usuarios con rol asignado y cifrado BCrypt (HU-01).
 */
@Service
public class RegistrarUsuarioUseCase {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordHasher passwordHasher;
    private final UsuarioMapper usuarioMapper;

    public RegistrarUsuarioUseCase(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordHasher passwordHasher,
            UsuarioMapper usuarioMapper
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordHasher = passwordHasher;
        this.usuarioMapper = usuarioMapper;
    }

    @Transactional
    public UsuarioResponse ejecutar(RegistrarUsuarioRequest request) {
        String codigoNormalizado = request.codigoUsuario().trim();
        Usuario.validarPasswordPlano(request.password());
        RolNombre rolNombre = RolNombre.fromString(request.rol());

        if (usuarioRepository.existsByCodigoUsuario(codigoNormalizado)) {
            throw new CodigoUsuarioDuplicadoException(codigoNormalizado);
        }

        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new RolNoEncontradoException(rolNombre.name()));

        String passwordHash = passwordHasher.hash(request.password());
        Usuario nuevoUsuario = Usuario.crearNuevo(
                codigoNormalizado,
                request.nombres(),
                request.apellidos(),
                passwordHash,
                rol
        );

        Usuario guardado = usuarioRepository.save(nuevoUsuario);
        return usuarioMapper.toResponse(guardado);
    }
}
