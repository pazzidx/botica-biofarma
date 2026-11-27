package com.biofarma.inventario.infrastructure.security;

import com.biofarma.inventario.domain.model.AuthenticatedUser;
import com.biofarma.inventario.domain.service.AuthenticatedUserProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Implementación de AuthenticatedUserProvider que extrae la identidad activa desde SecurityContextHolder.
 */
@Component
public class SecurityContextAuthenticatedUserProvider implements AuthenticatedUserProvider {

    @Override
    public AuthenticatedUser obtenerUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof UsuarioAutenticadoPrincipal principal)) {
            throw new AccessDeniedException("No existe una sesión autenticada válida.");
        }

        return new AuthenticatedUser(
                principal.idUsuario(),
                principal.codigoUsuario(),
                principal.rol()
        );
    }
}
