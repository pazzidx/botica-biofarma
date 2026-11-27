package com.biofarma.inventario.domain.service;

import com.biofarma.inventario.domain.model.Usuario;

/**
 * Puerto de dominio para emisión de tokens de autenticación.
 */
public interface TokenGenerator {

    String generarToken(Usuario usuario);
}
