package com.biofarma.inventario.application.usecase;

import com.biofarma.inventario.application.dto.LoginRequest;
import com.biofarma.inventario.application.dto.LoginResponse;
import com.biofarma.inventario.application.service.TokenProvider;
import com.biofarma.inventario.domain.exception.CredencialesInvalidasException;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import com.biofarma.inventario.domain.service.PasswordHasher;
import org.springframework.stereotype.Service;

/**
 * Caso de uso: Inicio de sesión de usuario con verificación de credenciales,
 * estado de cuenta activa y emisión de token JWT (HU-02 / BIO-30).
 */
@Service
public class IniciarSesionUseCase {

    private static final String MENSAJE_ERROR_CREDENCIALES = "Credenciales inválidas.";
    private static final String TIPO_TOKEN_BEARER = "Bearer";

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;

    public IniciarSesionUseCase(
            UsuarioRepository usuarioRepository,
            PasswordHasher passwordHasher,
            TokenProvider tokenProvider
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.tokenProvider = tokenProvider;
    }

    public LoginResponse ejecutar(LoginRequest request) {
        if (esSolicitudInvalida(request)) {
            throw new CredencialesInvalidasException(MENSAJE_ERROR_CREDENCIALES);
        }

        Usuario usuario = usuarioRepository.findByCodigoUsuario(request.codigoUsuario().trim())
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_ERROR_CREDENCIALES));

        if (!usuario.puedeAutenticarse()) {
            throw new CredencialesInvalidasException(MENSAJE_ERROR_CREDENCIALES);
        }

        if (!passwordHasher.matches(request.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException(MENSAJE_ERROR_CREDENCIALES);
        }

        String token = tokenProvider.generarToken(usuario);

        return new LoginResponse(
                token,
                TIPO_TOKEN_BEARER,
                tokenProvider.getExpiracionMs(),
                usuario.getIdUsuario(),
                usuario.getCodigoUsuario(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getRol().getNombre().name()
        );
    }

    private boolean esSolicitudInvalida(LoginRequest request) {
        return request == null
                || request.codigoUsuario() == null
                || request.codigoUsuario().isBlank()
                || request.password() == null
                || request.password().isBlank();
    }
}
