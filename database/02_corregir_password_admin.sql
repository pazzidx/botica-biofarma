-- ============================================================================
-- SCRIPT: 02_corregir_password_admin.sql
-- PROYECTO: Botica Biofarma
-- HISTORIA DE USUARIO: HU-02 (BIO-6) / SUBTAREA: BIO-30
-- OBJETIVO: Plantilla SQL segura para actualizar el hash vacío de la cuenta inicial 'admin'.
-- NOTA DE SEGURIDAD:
--   1. El backend inicializa automáticamente el hash de 'admin' al arrancar si se define
--      la variable de entorno INITIAL_ADMIN_PASSWORD en el servidor.
--   2. Si se prefiere actualizar directamente en SQL Server, reemplace @NuevoHashBCrypt
--      con el hash generado para su contraseña corporativa (ej. generado con BCrypt cost 10+).
-- ============================================================================

USE DB_BoticaBiofarma;
GO

DECLARE @NuevoHashBCrypt VARCHAR(255) = '$(ADMIN_BCRYPT_HASH)';

IF @NuevoHashBCrypt IS NOT NULL
   AND @NuevoHashBCrypt <> ''
   AND @NuevoHashBCrypt <> '$(ADMIN_BCRYPT_HASH)'
   AND LEN(@NuevoHashBCrypt) >= 50
BEGIN
    UPDATE Usuarios
    SET password_hash = @NuevoHashBCrypt,
        activo = 1
    WHERE codigo_usuario = 'admin'
      AND (password_hash IS NULL OR password_hash = '' OR LEN(password_hash) < 20);

    PRINT 'El hash BCrypt para el usuario administrador inicial (admin) ha sido actualizado correctamente.';
END
ELSE
BEGIN
    PRINT 'AVISO: Proporcione un hash BCrypt válido mediante el parámetro ADMIN_BCRYPT_HASH o defina INITIAL_ADMIN_PASSWORD en el backend.';
END
GO
