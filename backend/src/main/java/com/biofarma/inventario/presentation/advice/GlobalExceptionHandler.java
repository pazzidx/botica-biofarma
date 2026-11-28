package com.biofarma.inventario.presentation.advice;

import com.biofarma.inventario.domain.exception.AccesoVentaDenegadoException;
import com.biofarma.inventario.domain.exception.CodigoMedicamentoDuplicadoException;
import com.biofarma.inventario.domain.exception.CodigoUsuarioDuplicadoException;
import com.biofarma.inventario.domain.exception.CredencialesInvalidasException;
import com.biofarma.inventario.domain.exception.DatosInventarioInconsistentesException;
import com.biofarma.inventario.domain.exception.DatosMedicamentoInvalidosException;
import com.biofarma.inventario.domain.exception.DatosUsuarioInvalidosException;
import com.biofarma.inventario.domain.exception.MedicamentoNoEncontradoException;
import com.biofarma.inventario.domain.exception.OperacionNoPermitidaException;
import com.biofarma.inventario.domain.exception.RolNoEncontradoException;
import com.biofarma.inventario.domain.exception.UsuarioNoEncontradoException;
import com.biofarma.inventario.domain.exception.VentaNoEncontradaException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Manejador global de excepciones REST que traduce errores de dominio, validación y seguridad
 * a respuestas HTTP estandarizadas en español (ApiErrorResponse).
 * Cubre HU-01 / HU-02 (autenticación y autorización) y HU-04 (catálogo de medicamentos).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // -------------------------------------------------------------------------
    // Validación de entrada
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Datos inválidos",
                "Por favor revise los campos obligatorios del formulario.",
                request.getRequestURI(),
                fieldErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Solicitud inválida",
                "El cuerpo de la petición es inválido o tiene un formato JSON incorrecto.",
                request.getRequestURI(),
                Map.of()
        );
    }

    // -------------------------------------------------------------------------
    // HU-01 / HU-02 — Usuarios y seguridad
    // -------------------------------------------------------------------------

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(
            CredencialesInvalidasException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Credenciales inválidas",
                "El código de usuario o la contraseña son incorrectos.",
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ApiErrorResponse> handleJwtException(
            JwtException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Token inválido",
                "El token de sesión es inválido o ha expirado.",
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "No autorizado",
                "Debe iniciar sesión para acceder a este recurso.",
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(DatosUsuarioInvalidosException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidUserData(
            DatosUsuarioInvalidosException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validación de negocio",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(CodigoUsuarioDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateUserCode(
            CodigoUsuarioDuplicadoException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Código duplicado",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of("codigoUsuario", ex.getMessage())
        );
    }

    @ExceptionHandler(RolNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleRoleNotFound(
            RolNoEncontradoException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Rol no encontrado",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleUserNotFound(
            UsuarioNoEncontradoException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Usuario no encontrado",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<ApiErrorResponse> handleOperationNotAllowed(
            OperacionNoPermitidaException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Operación no permitida",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    // -------------------------------------------------------------------------
    // HU-04 — Catálogo de Medicamentos
    // -------------------------------------------------------------------------

    @ExceptionHandler(DatosMedicamentoInvalidosException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidMedicamentoData(
            DatosMedicamentoInvalidosException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validación de negocio",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(CodigoMedicamentoDuplicadoException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateMedicamentoCode(
            CodigoMedicamentoDuplicadoException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Código duplicado",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of("codigoProducto", ex.getMessage())
        );
    }

    @ExceptionHandler(MedicamentoNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> handleMedicamentoNotFound(
            MedicamentoNoEncontradoException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Medicamento no encontrado",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(DatosInventarioInconsistentesException.class)
    public ResponseEntity<ApiErrorResponse> handleInconsistentInventory(
            DatosInventarioInconsistentesException ex,
            HttpServletRequest request
    ) {
        log.error("Inconsistencia detectada en inventario durante consulta en {}: {}",
                request.getRequestURI(), ex.getMessage());
        return buildResponse(
                HttpStatus.CONFLICT,
                "Inconsistencia de inventario",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Parámetro inválido",
                "Uno de los parámetros enviados no tiene el formato esperado.",
                request.getRequestURI(),
                Map.of()
        );
    }

    // -------------------------------------------------------------------------
    // Infraestructura y catch-all
    // -------------------------------------------------------------------------

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Conflicto de integridad",
                "Ya existe un registro con los mismos datos únicos.",
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(AccesoVentaDenegadoException.class)
    public ResponseEntity<ApiErrorResponse> handleVentaAccessDenied(
            AccesoVentaDenegadoException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Acceso denegado",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(VentaNoEncontradaException.class)
    public ResponseEntity<ApiErrorResponse> handleVentaNotFound(
            VentaNoEncontradaException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Venta no encontrada",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Acceso denegado",
                "No tiene permisos para ejecutar esta operación.",
                request.getRequestURI(),
                Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Error inesperado procesando {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno",
                "Ocurrió un error interno al procesar la solicitud.",
                request.getRequestURI(),
                Map.of()
        );
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            String error,
            String message,
            String path,
            Map<String, String> fieldErrors
    ) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                path,
                fieldErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
