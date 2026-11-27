package com.biofarma.inventario.infrastructure.security;

import com.biofarma.inventario.domain.model.RolNombre;
import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
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
 * Filtro HTTP que valida el token JWT en Authorization: Bearer <token>, verifica el estado
 * activo del usuario en persistencia y puebla el SecurityContextHolder con
 * {@link UsuarioAutenticadoPrincipal} (HU-02 / HU-03).
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

        if (jwtService == null || authHeader == null || !authHeader.startsWith(TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(TOKEN_PREFIX.length()).trim();

        if (!jwt.isEmpty() && jwtService.esTokenValido(jwt)) {
            Claims claims = jwtService.extraerClaims(jwt);
            String codigoUsuario = claims.getSubject();

            if (codigoUsuario != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (usuarioRepository != null) {
                    Optional<Usuario> usuarioOpt = usuarioRepository.findByCodigoUsuario(codigoUsuario);
                    if (usuarioOpt.isPresent() && usuarioOpt.get().isActivo()) {
                        Usuario usuario = usuarioOpt.get();
                        establecerAutenticacion(
                                request,
                                usuario.getIdUsuario(),
                                usuario.getCodigoUsuario(),
                                usuario.getRol().getNombre()
                        );
                    } else {
                        SecurityContextHolder.clearContext();
                    }
                } else {
                    Number idClaim = claims.get("idUsuario", Number.class);
                    Integer idUsuario = idClaim != null ? idClaim.intValue() : null;
                    RolNombre rol = RolNombre.fromString(claims.get("rol", String.class));
                    establecerAutenticacion(request, idUsuario, codigoUsuario, rol);
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void establecerAutenticacion(
            HttpServletRequest request,
            Integer idUsuario,
            String codigoUsuario,
            RolNombre rol
    ) {
        UsuarioAutenticadoPrincipal principal = new UsuarioAutenticadoPrincipal(idUsuario, codigoUsuario, rol);
        List<SimpleGrantedAuthority> authorities = rol != null
                ? List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()))
                : List.of();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                authorities
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
