package com.biofarma.inventario.domain.repository;

import com.biofarma.inventario.domain.model.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para acceso y persistencia de usuarios.
 */
public interface UsuarioRepository {

    boolean existsByCodigoUsuario(String codigoUsuario);

    Usuario save(Usuario usuario);

    Optional<Usuario> findById(Integer idUsuario);

    Optional<Usuario> findByCodigoUsuario(String codigoUsuario);

    List<Usuario> findAll();

    void deleteById(Integer idUsuario);
}