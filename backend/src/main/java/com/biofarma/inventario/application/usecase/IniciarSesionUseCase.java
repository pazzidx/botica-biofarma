package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.LoginRequest;
import com.biofarma.inventario.application.dto.LoginResponse;
import com.biofarma.inventario.domain.exception.CredencialesInvalidasException;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import com.biofarma.inventario.domain.service.PasswordHasher;
import com.biofarma.inventario.domain.service.TokenGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso para autenticar usuarios con BCrypt y emitir token JWT.
 */
@Service
public class IniciarSesionUseCase {

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final TokenGenerator tokenGenerator;

    public IniciarSesionUseCase(
            UsuarioRepository usuarioRepository,
            PasswordHasher passwordHasher,
            TokenGenerator tokenGenerator
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.tokenGenerator = tokenGenerator;
    }

    @Transactional(readOnly = true)
    public LoginResponse ejecutar(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCodigoUsuario(request.codigoUsuario().trim())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.isActivo() || !passwordHasher.matches(request.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        String token = tokenGenerator.generarToken(usuario);
        String nombreCompleto = (usuario.getNombres() + " " + usuario.getApellidos()).trim();

        return new LoginResponse(
                token,
                new LoginResponse.UsuarioAutenticadoDTO(
                        usuario.getIdUsuario(),
                        usuario.getCodigoUsuario(),
                        nombreCompleto,
                        usuario.getRol().getNombre().name()
                )
        );
    }
}
