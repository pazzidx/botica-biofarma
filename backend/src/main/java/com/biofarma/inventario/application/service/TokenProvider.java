package com.biofarma.inventario.application.service;

import com.biofarma.inventario.domain.model.Usuario;

/**
 * Puerto para la generación y gestión de tokens de sesión de usuario (HU-02 / BIO-30).
 */
public interface TokenProvider {

    String generarToken(Usuario usuario);

    long getExpiracionMs();
}
