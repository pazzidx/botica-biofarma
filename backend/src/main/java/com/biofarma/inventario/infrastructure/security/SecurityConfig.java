package com.biofarma.inventario.infrastructure.security;

import com.biofarma.inventario.domain.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración de seguridad Spring Security con RBAC y JWT sin estado (HU-01, HU-02 / BIO-31).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Declara un UserDetailsService explícito que documenta y asegura que la API no utiliza
     * autenticación tradicional basada en sesiones o formulario, sino tokens JWT sin estado.
     * Esto evita que Spring Security genere una contraseña aleatoria de desarrollo en consola
     * y garantiza que el único mecanismo de autenticación sea el filtro JWT.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException(
                    "Autenticación exclusiva vía JWT: La API de Botica Biofarma no utiliza UserDetailsService tradicional."
            );
        };
    }

    @Bean
    public JwtService jwtService(
            @Value("${jwt.secret:}") String secretKey,
            @Value("${jwt.expiration:28800000}") long expirationMs
    ) {
        return new JwtService(secretKey, expirationMs);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService,
            ObjectProvider<UsuarioRepository> usuarioRepositoryProvider
    ) {
        return new JwtAuthenticationFilter(jwtService, usuarioRepositoryProvider.getIfAvailable());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Endpoint público de autenticación
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        // 2. Endpoint de cierre de sesión: accesible para cualquier usuario autenticado
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()
                        // 3. Gestión de usuarios: Restringido EXCLUSIVAMENTE a rol ADMINISTRADORA (BIO-31)
                        .requestMatchers("/api/v1/usuarios/**").hasRole("ADMINISTRADORA")
                        // 4. Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(String.format(
                                    "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"No autorizado\",\"message\":\"Debe iniciar sesión para acceder a este recurso.\",\"path\":\"%s\",\"fieldErrors\":{}}",
                                    LocalDateTime.now(),
                                    request.getRequestURI()
                            ));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(String.format(
                                    "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Acceso denegado\",\"message\":\"No tiene permisos suficientes para realizar esta operación.\",\"path\":\"%s\",\"fieldErrors\":{}}",
                                    LocalDateTime.now(),
                                    request.getRequestURI()
                            ));
                        })
                )
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://127.0.0.1:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
