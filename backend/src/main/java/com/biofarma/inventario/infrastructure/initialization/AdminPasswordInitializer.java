package com.biofarma.inventario.infrastructure.initialization;

import com.biofarma.inventario.domain.model.Usuario;
import com.biofarma.inventario.domain.repository.UsuarioRepository;
import com.biofarma.inventario.domain.service.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializador seguro de contraseña para la cuenta administradora inicial (HU-02 / BIO-30).
 * Solo se ejecuta si se suministra explícitamente la variable de entorno INITIAL_ADMIN_PASSWORD.
 * Evita contraseñas predeterminadas conocidas en código fuente.
 */
@Component
public class AdminPasswordInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminPasswordInitializer.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordHasher passwordHasher;
    private final String initialAdminPassword;

    public AdminPasswordInitializer(
            UsuarioRepository usuarioRepository,
            PasswordHasher passwordHasher,
            @Value("${app.security.initial-admin-password:}") String initialAdminPassword
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordHasher = passwordHasher;
        this.initialAdminPassword = initialAdminPassword != null ? initialAdminPassword.trim() : "";
    }

    @Override
    public void run(ApplicationArguments args) {
        if (initialAdminPassword.isBlank()) {
            log.info("INITIAL_ADMIN_PASSWORD no está configurada. Se omite auto-inicialización de contraseña admin.");
            return;
        }

        try {
            usuarioRepository.findByCodigoUsuario("admin").ifPresent(admin -> {
                String currentHash = admin.getPasswordHash();
                if (currentHash == null || currentHash.isBlank() || currentHash.length() < 20) {
                    log.info("Detectado usuario admin con password_hash vacío o inválido. Inicializando hash seguro desde variable de entorno...");
                    String newHash = passwordHasher.hash(initialAdminPassword);
                    Usuario actualizado = Usuario.reconstruir(
                            admin.getIdUsuario(),
                            admin.getCodigoUsuario(),
                            admin.getNombres(),
                            admin.getApellidos(),
                            newHash,
                            admin.getRol(),
                            admin.isActivo(),
                            admin.getFechaCreacion()
                    );
                    usuarioRepository.save(actualizado);
                    log.info("Hash BCrypt para usuario admin inicializado exitosamente.");
                }
            });
        } catch (Exception ex) {
            log.warn("No se pudo verificar o inicializar el hash admin en base de datos: {}", ex.getMessage());
        }
    }
}
