package com.biofarma.inventario.domain.service;

/**
 * Puerto de dominio para cifrado y verificación segura de contraseñas.
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
