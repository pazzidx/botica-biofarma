package com.biofarma.inventario.infrastructure.security;

import com.biofarma.inventario.domain.service.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Adaptador de cifrado de contraseñas respaldado por {@link PasswordEncoder} (BCrypt).
 */
@Component
public class BCryptPasswordHasherAdapter implements PasswordHasher {

    private final PasswordEncoder passwordEncoder;

    public BCryptPasswordHasherAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
