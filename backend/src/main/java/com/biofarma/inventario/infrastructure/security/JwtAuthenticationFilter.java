package com.biofarma.inventario.infrastructure.security;

import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro HTTP que intercepta cada solicitud, valida el token JWT en el encabezado
 * Authorization: Bearer <token>, verifica el estado activo del usuario en persistencia
 * y carga la autenticación en el SecurityContextHolder (HU-02 / BIO-30).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HEADER_AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(TOKEN_PREFIX.length()).trim();

        if (jwtService.esTokenValido(jwt)) {
            String codigoUsuario = jwtService.extraerCodigoUsuario(jwt);

            if (codigoUsuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (usuarioRepository != null) {
                    Optional<Usuario> usuarioOpt = usuarioRepository.findByCodigoUsuario(codigoUsuario);
                    if (usuarioOpt.isPresent() && usuarioOpt.get().isActivo()) {
                        Usuario usuario = usuarioOpt.get();
                        String rol = usuario.getRol().getNombre().name();
                        establecerAutenticacion(request, usuario.getCodigoUsuario(), rol);
                    } else {
                        // Usuario inexistente o desactivado: revocar inmediatamente el acceso aunque el token sea criptográficamente válido
                        SecurityContextHolder.clearContext();
                    }
                } else {
                    // Fallback para entornos donde el repositorio no se encuentre inyectado en el filtro
                    String rol = jwtService.extraerRol(jwt);
                    establecerAutenticacion(request, codigoUsuario, rol);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void establecerAutenticacion(HttpServletRequest request, String codigoUsuario, String rol) {
        List<SimpleGrantedAuthority> authorities = rol != null
                ? List.of(new SimpleGrantedAuthority("ROLE_" + rol))
                : List.of();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                codigoUsuario,
                null,
                authorities
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
