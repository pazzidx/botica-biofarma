package com.biofarma.inventario.domain.model;

import com.biofarma.inventario.domain.exception.DatosUsuarioInvalidosException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Entidad pura de dominio que encapsula las reglas de negocio de un usuario de Botica Biofarma.
 */
public class Usuario {

    private static final int MAX_CODIGO_LENGTH = 20;
    private static final int MIN_CODIGO_LENGTH = 3;
    private static final int MAX_NOMBRE_LENGTH = 100;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 100;
    private static final Pattern CODIGO_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");

    private final Integer idUsuario;
    private final String codigoUsuario;
    private final String nombres;
    private final String apellidos;
    private final String passwordHash;
    private final Rol rol;
    private final boolean activo;
    private final LocalDateTime fechaCreacion;

    private Usuario(
            Integer idUsuario,
            String codigoUsuario,
            String nombres,
            String apellidos,
            String passwordHash,
            Rol rol,
            boolean activo,
            LocalDateTime fechaCreacion
    ) {
        this.idUsuario = idUsuario;
        this.codigoUsuario = codigoUsuario;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * Valida la contraseña en texto plano antes de aplicar cifrado.
     *
     * @param passwordPlano contraseña sin cifrar
     */
    public static void validarPasswordPlano(String passwordPlano) {
        if (passwordPlano == null || passwordPlano.isBlank()) {
            throw new DatosUsuarioInvalidosException("La contraseña del usuario es obligatoria.");
        }
        if (passwordPlano.length() < MIN_PASSWORD_LENGTH || passwordPlano.length() > MAX_PASSWORD_LENGTH) {
            throw new DatosUsuarioInvalidosException(
                    "La contraseña debe tener entre " + MIN_PASSWORD_LENGTH + " y " + MAX_PASSWORD_LENGTH + " caracteres."
            );
        }
    }

    /**
     * Crea una nueva instancia de usuario validando todas sus invariantes de negocio.
     */
    public static Usuario crearNuevo(
            String codigoUsuario,
            String nombres,
            String apellidos,
            String passwordHash,
            Rol rol
    ) {
        String codigoNormalizado = validarCodigoUsuario(codigoUsuario);
        String nombresNormalizados = validarTextoObligatorio(nombres, "Los nombres", MAX_NOMBRE_LENGTH);
        String apellidosNormalizados = validarTextoObligatorio(apellidos, "Los apellidos", MAX_NOMBRE_LENGTH);

        if (passwordHash == null || passwordHash.isBlank()) {
            throw new DatosUsuarioInvalidosException("El hash de contraseña no puede estar vacío.");
        }
        if (rol == null) {
            throw new DatosUsuarioInvalidosException("El rol asignado al usuario es obligatorio.");
        }

        return new Usuario(
                null,
                codigoNormalizado,
                nombresNormalizados,
                apellidosNormalizados,
                passwordHash,
                rol,
                true,
                LocalDateTime.now()
        );
    }

    /**
     * Reconstruye un usuario existente desde persistencia.
     */
    public static Usuario reconstruir(
            Integer idUsuario,
            String codigoUsuario,
            String nombres,
            String apellidos,
            String passwordHash,
            Rol rol,
            boolean activo,
            LocalDateTime fechaCreacion
    ) {
        return new Usuario(
                idUsuario,
                codigoUsuario,
                nombres,
                apellidos,
                passwordHash,
                rol,
                activo,
                fechaCreacion
        );
    }

    private static String validarCodigoUsuario(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new DatosUsuarioInvalidosException("El código de usuario es obligatorio.");
        }
        String normalizado = codigo.trim();
        if (normalizado.length() < MIN_CODIGO_LENGTH || normalizado.length() > MAX_CODIGO_LENGTH) {
            throw new DatosUsuarioInvalidosException(
                    "El código de usuario debe tener entre " + MIN_CODIGO_LENGTH + " y " + MAX_CODIGO_LENGTH + " caracteres."
            );
        }
        if (!CODIGO_PATTERN.matcher(normalizado).matches()) {
            throw new DatosUsuarioInvalidosException(
                    "El código de usuario solo puede contener letras, números, puntos, guiones y guion bajo."
            );
        }
        return normalizado;
    }

    private static String validarTextoObligatorio(String valor, String campo, int maxLength) {
        if (valor == null || valor.isBlank()) {
            throw new DatosUsuarioInvalidosException(campo + " son obligatorios.");
        }
        String normalizado = valor.trim();
        if (normalizado.length() > maxLength) {
            throw new DatosUsuarioInvalidosException(
                    campo + " no pueden superar los " + maxLength + " caracteres."
            );
        }
        return normalizado;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public String getCodigoUsuario() {
        return codigoUsuario;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario usuario)) return false;
        return Objects.equals(idUsuario, usuario.idUsuario)
                && Objects.equals(codigoUsuario, usuario.codigoUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, codigoUsuario);
    }
}
