package com.biofarma.inventario.application.dto;

/**
 * Respuesta de eliminación inteligente de usuario (borrado físico sin ventas vs. baja lógica con ventas).
 */
public record EliminacionUsuarioResponse(
        Integer idUsuario,
        boolean eliminadoFisicamente,
        String mensaje,
        UsuarioResponse usuario
) {
}
