package com.biofarma.inventario.infrastructure.security;

import com.biofarma.inventario.application.service.TokenProvider;
import com.biofarma.inventario.domain.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;

/**
 * Componente de infraestructura para la generación, firma y validación de tokens JWT
 * utilizando JJWT y clave segura HMAC-SHA256 (HU-02 / BIO-29 / BIO-30 / BIO-31).
 */
public class JwtService implements TokenProvider {

    private final String secretKeyString;
    private final long expirationMs;

    public JwtService(
            String secretKeyString,
            long expirationMs
    ) {
        if (secretKeyString == null || secretKeyString.isBlank() || secretKeyString.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException(
                    "La clave secreta JWT (JWT_SECRET) es obligatoria y debe tener al menos 32 caracteres (256 bits)."
            );
        }
        this.secretKeyString = secretKeyString;
        this.expirationMs = expirationMs;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKeyString.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String generarToken(Usuario usuario) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("idUsuario", usuario.getIdUsuario());
        extraClaims.put("codigoUsuario", usuario.getCodigoUsuario());
        extraClaims.put("nombres", usuario.getNombres());
        extraClaims.put("apellidos", usuario.getApellidos());
        extraClaims.put("rol", usuario.getRol().getNombre().name());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(usuario.getCodigoUsuario())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public long getExpiracionMs() {
        return expirationMs;
    }

    public String extraerCodigoUsuario(String token) {
        return extraerClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        Claims claims = extraerClaims(token);
        return claims.get("rol", String.class);
    }

    /**
     * Extrae la fecha exacta de expiración contenida en los claims del token.
     *
     * @param token Token JWT firmado en formato compacto.
     * @return Objeto {@link Date} con la marca temporal de expiración.
     * @throws IllegalArgumentException si el token es nulo o está en blanco.
     * @throws io.jsonwebtoken.JwtException si la firma no es válida, el token está corrupto o expirado.
     */
    public Date extraerFechaExpiracion(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token no puede ser nulo ni vacío.");
        }
        return extraerClaims(token).getExpiration();
    }

    public boolean esTokenValido(String token) {
        try {
            Claims claims = extraerClaims(token);
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public boolean esTokenValido(String token, String codigoUsuarioEsperado) {
        try {
            Claims claims = extraerClaims(token);
            String codigoUsuario = claims.getSubject();
            Date expiration = claims.getExpiration();
            boolean noExpirado = expiration != null && expiration.after(new Date());
            return noExpirado && codigoUsuario != null && codigoUsuario.equalsIgnoreCase(codigoUsuarioEsperado);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
