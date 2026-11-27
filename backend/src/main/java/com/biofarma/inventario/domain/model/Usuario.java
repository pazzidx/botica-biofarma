package com.biofarma.inventario.domain.model;

import com.biofarma.inventario.domain.exception.DatosUsuarioInvalidosException;
import com.biofarma.inventario.domain.exception.OperacionNoPermitidaException;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Entidad pura de dominio que encapsula las reglas de negocio de un usuario de Botica Biofarma.
 */
public class Usuario {

    private static final int MAX_CODIGO_LENGTH = 30;
    private static final int MIN_CODIGO_LENGTH = 3;
    private static final int MAX_NOMBRE_LENGTH = 100;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_PASSWORD_LENGTH = 100;
    private static final Pattern CODIGO_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");
    private static final Pattern SOLO_LETRAS_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$");

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
        String nombresNormalizados = validarSoloLetras(nombres, "Los nombres");
        String apellidosNormalizados = validarSoloLetras(apellidos, "Los apellidos");

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

    /**
     * Indica si la cuenta está protegida frente a edición/eliminación por ser la única administradora.
     */
    public boolean esProtegido() {
        return "admin".equalsIgnoreCase(this.codigoUsuario)
                || (this.rol != null && this.rol.getNombre() == RolNombre.ADMINISTRADORA);
    }

    /**
     * Actualiza únicamente nombres y apellidos de una trabajadora manteniendo inalterables código, rol y estado.
     */
    public Usuario actualizarDatosPersonales(String nuevosNombres, String nuevosApellidos) {
        if (esProtegido()) {
            throw new OperacionNoPermitidaException(
                    "No está permitido modificar la cuenta administradora principal."
            );
        }
        String nombresValidados = validarSoloLetras(nuevosNombres, "Los nombres");
        String apellidosValidados = validarSoloLetras(nuevosApellidos, "Los apellidos");

        return new Usuario(
                this.idUsuario,
                this.codigoUsuario,
                nombresValidados,
                apellidosValidados,
                this.passwordHash,
                this.rol,
                this.activo,
                this.fechaCreacion
        );
    }

    /**
     * Aplica baja lógica al usuario (estado inactivo).
     */
    public Usuario desactivar() {
        if (esProtegido()) {
            throw new OperacionNoPermitidaException(
                    "No está permitido desactivar o eliminar la cuenta administradora principal."
            );
        }
        return new Usuario(
                this.idUsuario,
                this.codigoUsuario,
                this.nombres,
                this.apellidos,
                this.passwordHash,
                this.rol,
                false,
                this.fechaCreacion
        );
    }

    /**
     * Reactiva una cuenta inactiva.
     */
    public Usuario activar() {
        return new Usuario(
                this.idUsuario,
                this.codigoUsuario,
                this.nombres,
                this.apellidos,
                this.passwordHash,
                this.rol,
                true,
                this.fechaCreacion
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

    private static String validarSoloLetras(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DatosUsuarioInvalidosException(campo + " son obligatorios.");
        }
        String normalizado = valor.trim();
        if (normalizado.length() > MAX_NOMBRE_LENGTH) {
            throw new DatosUsuarioInvalidosException(
                    campo + " no pueden superar los " + MAX_NOMBRE_LENGTH + " caracteres."
            );
        }
        if (!SOLO_LETRAS_PATTERN.matcher(normalizado).matches()) {
            throw new DatosUsuarioInvalidosException(
                    campo + " solo pueden contener letras, tildes, ñ y espacios."
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

    /**
     * Determina si la cuenta de usuario se encuentra en condiciones válidas para autenticarse.
     * Una cuenta inactiva o sin un hash de contraseña configurado no puede autenticarse.
     *
     * @return true si la cuenta está activa y posee un hash de contraseña no vacío
     */
    public boolean puedeAutenticarse() {
        return this.activo && this.passwordHash != null && !this.passwordHash.isBlank();
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
