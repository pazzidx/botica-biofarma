package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.RegistrarUsuarioRequest;
import com.biofarma.inventario.application.dto.UsuarioResponse;
import com.biofarma.inventario.application.mapper.UsuarioMapper;
import com.biofarma.inventario.domain.exception.CodigoUsuarioDuplicadoException;
import com.biofarma.inventario.domain.exception.DatosUsuarioInvalidosException;
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
 * Caso de uso para el registro seguro de trabajadoras con cifrado BCrypt (HU-01).
 * Fuerza el rol único TRABAJADORA e impide registrar cuentas ADMINISTRADORA adicionales.
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

        if (request.rol() != null && !request.rol().isBlank()) {
            RolNombre rolSolicitado = RolNombre.fromString(request.rol());
            if (rolSolicitado != RolNombre.TRABAJADORA) {
                throw new DatosUsuarioInvalidosException(
                        "Solo se permite registrar cuentas con rol TRABAJADORA. El sistema mantiene una única cuenta ADMINISTRADORA."
                );
            }
        }

        if (usuarioRepository.existsByCodigoUsuario(codigoNormalizado)) {
            throw new CodigoUsuarioDuplicadoException(codigoNormalizado);
        }

        Rol rolTrabajadora = rolRepository.findByNombre(RolNombre.TRABAJADORA)
                .orElseThrow(() -> new RolNoEncontradoException(RolNombre.TRABAJADORA.name()));

        String passwordHash = passwordHasher.hash(request.password());
        Usuario nuevoUsuario = Usuario.crearNuevo(
                codigoNormalizado,
                request.nombres(),
                request.apellidos(),
                passwordHash,
                rolTrabajadora
        );

        Usuario guardado = usuarioRepository.save(nuevoUsuario);
        return usuarioMapper.toResponse(guardado);
    }
}
