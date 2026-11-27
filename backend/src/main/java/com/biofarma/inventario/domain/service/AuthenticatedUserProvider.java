package com.biofarma.inventario.domain.service;

import com.biofarma.inventario.domain.model.AuthenticatedUser;

/**
 * Puerto de dominio para obtener la identidad del usuario autenticado en la petición actual.
 */
public interface AuthenticatedUserProvider {

    AuthenticatedUser obtenerUsuarioAutenticado();
}
