package com.biofarma.inventario.infrastructure.security;

import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.service.TokenGenerator;
import org.springframework.stereotype.Component;

/**
 * Adaptador de infraestructura que implementa TokenGenerator usando JwtService.
 */
@Component
public class JwtTokenGeneratorAdapter implements TokenGenerator {

    private final JwtService jwtService;

    public JwtTokenGeneratorAdapter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String generarToken(Usuario usuario) {
        return jwtService.generarToken(
                usuario.getIdUsuario(),
                usuario.getCodigoUsuario(),
                usuario.getRol().getNombre().name()
        );
    }
}
