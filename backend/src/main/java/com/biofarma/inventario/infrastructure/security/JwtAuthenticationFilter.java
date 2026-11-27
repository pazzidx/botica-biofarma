package com.biofarma.inventario.infrastructure.security;

import com.biofarma.inventario.domain.model.RolNombre;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro JWT que valida el token Bearer y puebla el SecurityContextHolder con la identidad y rol del usuario.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(ObjectProvider<JwtService> jwtServiceProvider) {
        this.jwtService = jwtServiceProvider.getIfAvailable();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (jwtService == null || authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        if (!token.isEmpty()
                && SecurityContextHolder.getContext().getAuthentication() == null
                && jwtService.esTokenValido(token)) {
            Claims claims = jwtService.extraerClaims(token);
            Integer idUsuario = claims.get("idUsuario", Number.class).intValue();
            String codigoUsuario = claims.getSubject();
            RolNombre rol = RolNombre.fromString(claims.get("rol", String.class));

            UsuarioAutenticadoPrincipal principal = new UsuarioAutenticadoPrincipal(idUsuario, codigoUsuario, rol);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
