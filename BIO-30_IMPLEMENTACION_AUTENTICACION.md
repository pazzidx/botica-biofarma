# Informe de Auditoría e Implementación Técnica — BIO-29 y BIO-30
**Sistema Web de Control de Inventario y Ventas — Botica Biofarma**
**Historia de Usuario:** HU-02 (BIO-6): Inicio y cierre de sesión con control de acceso por rol
**Subtareas:**
- **BIO-29 (ST-02.1):** Configurar Spring Security y generación/validación de tokens JWT
- **BIO-30 (ST-02.2):** Implementar servicio y endpoint de inicio y cierre de sesión
**Fecha de Auditoría:** 26 de Noviembre de 2026
**Auditor / Arquitecto:** Antigravity (Senior Software Architect & Fullstack Engineer)

---

## 1. Objetivo de la Auditoría y Subtarea
Auditar y consolidar de manera rigurosa la base de seguridad de **BIO-29** y el desarrollo completo de la subtarea **BIO-30** (*servicio y endpoints REST de inicio y cierre de sesión*), verificando la conformidad arquitectónica con **Clean Architecture**, la ausencia de secretos o contraseñas en código duro, la vigencia JWT configurable (propuesta de 8 horas para turno farmacéutico en el MVP académico), la revocación de acceso a usuarios desactivados, el comportamiento del cierre de sesión stateless y la ejecución verificable de 26 pruebas automáticas sin regresiones sobre la HU-01.

---

## 2. Inspección del Entorno y Estado Git
Se ejecutaron los comandos de auditoría en la raíz del proyecto:
- **Ubicación exacta:** `C:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma` (`git rev-parse --show-toplevel`)
- **Remoto configurado:** `origin https://github.com/pazzidx/botica-biofarma.git` (fetch/push) (`git remote -v`)
- **Rama actual:** `main` (`git branch --show-current`)
- **Estado de working tree:** Archivos listos para commit separados según correspondencia funcional de BIO-29 y BIO-30 (`git status`).
- **Historial reciente (`git log --oneline -10`):**
  - `0562f81 Merge pull request #1 from pazzidx/feature/BIO-01-miler`
  - `15f70d1 feat(HU-01): registro de usuarios y configuracion naming strategy Hibernate`
  - `c03c342 refactor: reorganizar estructura del proyecto aplicando arquitectura limpia`
  - `aed3d04 BIO-5 feat(frontend): inicializar proyecto base React con Vite y Axios`
  - `3646e9e BIO-5 feat(backend): inicializar Spring Boot 4 con Clean Architecture y conexion SQL Server`
  - `696c8ea BIO-5 feat(bd): crear esquema inicial SQL Server DB_BoticaBiofarma`
  - `47f6e80 BIO-5 estructura inicial del repositorio`
  - `9ce29c5 Initial commit`

---

## 3. Verificación y Resolución de BIO-29
Al auditar los entregables de BIO-29 se confirmó que:
1. **Lo existente:** `application.properties` con propiedades base y `SecurityConfig.java` con codificador BCrypt y sesiones `STATELESS`.
2. **Carencias identificadas:** Faltaban las librerías JJWT en `pom.xml`, el utilitario de tokens `JwtService`, el filtro `JwtAuthenticationFilter` y la verificación de usuario activo en persistencia.
3. **Correcciones aplicadas:** Se agregaron las dependencias oficiales `io.jsonwebtoken:jjwt-api:0.12.6`, `jjwt-impl:0.12.6` y `jjwt-jackson:0.12.6`. Se implementó `JwtService` y `JwtAuthenticationFilter` (con validación de estado activo en base de datos) y se vincularon como beans en `SecurityConfig`.

---

## 4. Clasificación y Mapeo de Archivos para Commits (BIO-29 vs BIO-30)

Para mantener un historial de Git limpio y modular, los archivos modificados y creados se clasifican de la siguiente manera:

### Bloque A: Subtarea BIO-29 (Configuración Spring Security y JWT)
- `backend/pom.xml` (dependencias JJWT y H2 de prueba)
- `backend/src/main/resources/application.properties` (variables de entorno seguras JWT y expiración de 8h)
- `backend/src/main/java/com/biofarma/inventario/application/service/TokenProvider.java`
- `backend/src/main/java/com/biofarma/inventario/infrastructure/security/JwtService.java`
- `backend/src/main/java/com/biofarma/inventario/infrastructure/security/JwtAuthenticationFilter.java`
- `backend/src/main/java/com/biofarma/inventario/infrastructure/security/SecurityConfig.java`
- `backend/src/test/java/com/biofarma/inventario/infrastructure/security/JwtServiceTest.java`
- `backend/src/test/resources/application.properties`

### Bloque B: Subtarea BIO-30 (Servicio y Endpoints de Inicio/Cierre de Sesión)
- `backend/src/main/java/com/biofarma/inventario/domain/exception/CredencialesInvalidasException.java`
- `backend/src/main/java/com/biofarma/inventario/application/dto/LoginRequest.java`
- `backend/src/main/java/com/biofarma/inventario/application/dto/LoginResponse.java`
- `backend/src/main/java/com/biofarma/inventario/application/dto/LogoutResponse.java`
- `backend/src/main/java/com/biofarma/inventario/application/usecase/IniciarSesionUseCase.java`
- `backend/src/main/java/com/biofarma/inventario/application/usecase/CerrarSesionUseCase.java`
- `backend/src/main/java/com/biofarma/inventario/infrastructure/initialization/AdminPasswordInitializer.java`
- `backend/src/main/java/com/biofarma/inventario/presentation/controller/AuthController.java`
- `backend/src/main/java/com/biofarma/inventario/presentation/advice/GlobalExceptionHandler.java`
- `backend/src/test/java/com/biofarma/inventario/application/usecase/IniciarSesionUseCaseTest.java`
- `backend/src/test/java/com/biofarma/inventario/application/usecase/CerrarSesionUseCaseTest.java`
- `backend/src/test/java/com/biofarma/inventario/presentation/controller/AuthControllerTest.java`
- `database/02_corregir_password_admin.sql`
- `BIO-30_IMPLEMENTACION_AUTENTICACION.md`

---

## 5. Explicación de los Casos de Uso

### `IniciarSesionUseCase`
- Valida los parámetros de entrada (`LoginRequest`).
- Recupera al usuario mediante `UsuarioRepository.findByCodigoUsuario(codigoNormalizado)`.
- Si el usuario no existe o `usuario.isActivo() == false`, arroja `CredencialesInvalidasException("Credenciales inválidas.")`.
- Valida la contraseña mediante `PasswordHasher.matches(passwordPlano, usuario.getPasswordHash())` (algoritmo BCrypt). Si es errónea, arroja el mismo mensaje genérico para mitigar ataques de enumeración.
- Emite el token JWT a través de `TokenProvider.generarToken(usuario)` con claims de identidad y rol.
- Devuelve `LoginResponse` con vigencia en milisegundos y datos públicos del usuario. **Bajo ninguna circunstancia expone contraseñas ni hashes**.

### `CerrarSesionUseCase`
- Genera la respuesta confirmatoria `LogoutResponse` con mensaje `"Sesión cerrada exitosamente."` y marca de tiempo.
- **Principio Stateless:** En una arquitectura JWT sin estado no existen tablas de sesión ni revocación anticipada en el servidor (salvo listas negras en memoria que violarían el estándar ligero). El cierre de sesión confirma la terminación en servidor y encomienda al cliente (React / `AuthContext` en BIO-32) la purga del token del `localStorage` y la redirección a la vista de login.

---

## 6. Endpoints REST Implementados

| Método | Endpoint | Seguridad | Propósito |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/v1/auth/login` | Público (`permitAll()`) | Autentica al usuario y devuelve el token JWT y datos de rol. |
| **POST** | `/api/v1/auth/logout` | Autenticado (`authenticated()`) | Confirma el cierre de sesión local de un usuario autenticado. |

---

## 7. Ejemplos de Peticiones y Respuestas

### 7.1. Login Exitoso (Vigencia 8 Horas / Turno Laboral)
- **POST** `/api/v1/auth/login`
```json
{
  "codigoUsuario": "admin",
  "password": "Admin123*"
}
```
- **Respuesta 200 OK:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJpZFVzdWFyaW8iOjEsImNvZGlnb1VzdWFyaW8iOiJhZG1pbiIsIm5vbWJyZXMiOiJQcm9waWV0YXJpYSIsImFwZWxsaWRvcyI6IkJpb2Zhcm1hIiwicm9sIjoiQURNSU5JU1RSQURPUkEiLCJzdWIiOiJhZG1pbiIsImlhdCI6MTczMjYyMzAwMCwiZXhwIjoxNzMyNjUxODAwfQ...",
  "tipoToken": "Bearer",
  "expiraEnMs": 28800000,
  "idUsuario": 1,
  "codigoUsuario": "admin",
  "nombres": "Propietaria",
  "apellidos": "Biofarma",
  "rol": "ADMINISTRADORA"
}
```

### 7.2. Login con Credenciales Erróneas o Cuenta Inactiva
- **POST** `/api/v1/auth/login`
- **Respuesta 401 Unauthorized:**
```json
{
  "timestamp": "2026-11-26T09:48:12.450",
  "status": 401,
  "error": "Credenciales inválidas",
  "message": "El código de usuario o la contraseña son incorrectos.",
  "path": "/api/v1/auth/login",
  "fieldErrors": {}
}
```

### 7.3. Cierre de Sesión Autenticado
- **POST** `/api/v1/auth/logout` (Header: `Authorization: Bearer <token_valido>`)
- **Respuesta 200 OK:**
```json
{
  "mensaje": "Sesión cerrada exitosamente.",
  "timestamp": "2026-11-26T09:48:13.120"
}
```

### 7.4. Cierre de Sesión Rechazado por Token Inexistente
- **POST** `/api/v1/auth/logout` (Sin Header)
- **Respuesta 401 Unauthorized:**
```json
{
  "status": 401,
  "error": "No autorizado",
  "message": "Debe iniciar sesión para acceder a este recurso."
}
```

---

## 8. Verificación de Seguridad y Hallazgos Resueltos

1. **Gestión de Secretos y Variables de Entorno (Requisito 4):**
   - Se refactorizó `application.properties` para enlazar la clave de firma JWT a la variable de entorno `${JWT_SECRET}` y la vigencia a `${JWT_EXPIRATION_MS}`.
   - En `AdminPasswordInitializer.java`, se eliminaron contraseñas predeterminadas codificadas; el inicializador se activa **únicamente** si se suministra explícitamente la variable `${INITIAL_ADMIN_PASSWORD}`.
2. **Vigencia Configurable del Token (Requisito 6):**
   - Se configuró la vigencia predeterminada en **8 horas** (`28,800,000 ms`), ideal para el turno de trabajo de una botica en el MVP académico, totalmente parametrizable mediante `JWT_EXPIRATION_MS`.
3. **Revocación de Acceso a Usuarios Desactivados (Requisito 8):**
   - En `JwtAuthenticationFilter`, se implementó la verificación de identidad contra `UsuarioRepository`. Si un usuario posee un token JWT con firma y expiración vigentes, pero su cuenta fue desactivada (`activo == false`), el filtro rechaza la autenticación de inmediato (`SecurityContextHolder.clearContext()`), devolviendo `401 Unauthorized` ante cualquier intento de acceso a recursos protegidos.
4. **Protección de la Inicialización en Base de Datos (Requisito 5):**
   - Se transformó `02_corregir_password_admin.sql` en una plantilla segura que no inserta contraseñas fijas, dejando la auto-inicialización bajo control estricto de variables de entorno del servidor backend.

---

## 9. Auditoría de Conexión Real con SQL Server

- **Detección de Instancias:**
  - `localhost\ABD_GRUPO2`: Microsoft SQL Server 2025 Enterprise Developer Edition (en ejecución).
  - `localhost\SQLEXPRESS`: Microsoft SQL Server 2025 Express Edition (en ejecución).
- **Estado de la Base de Datos:**
  - Se comprobó mediante `sqlcmd` con autenticación de Windows (`-E -C`) que `DB_ID('DB_BoticaBiofarma')` es `NULL` en ambas instancias. La base de datos aún no ha sido creada en el servidor local.
  - Conforme al principio de no ejecutar operaciones destructivas ni alterar la base de datos sin instrucción explícita, se conservaron intactos los esquemas y se verificó que la suite automatizada corra de forma 100% aislada con H2 (`scope: test`).

---

## 10. Evidencias Verificables de Pruebas Automáticas

Se ejecutó la suite completa de pruebas con Maven y OpenJDK 21.0.8:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.biofarma.inventario.application.usecase.CerrarSesionUseCaseTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.biofarma.inventario.application.usecase.IniciarSesionUseCaseTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.biofarma.inventario.application.usecase.RegistrarUsuarioUseCaseTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.biofarma.inventario.BackendApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.biofarma.inventario.infrastructure.security.JwtServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.biofarma.inventario.presentation.controller.AuthControllerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.biofarma.inventario.presentation.controller.UsuarioControllerTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO] Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Casos Específicos Evaluados:
1. `IniciarSesionUseCaseTest`:
   - Inicio exitoso con rol `ADMINISTRADORA`.
   - Inicio exitoso con rol `TRABAJADORA`.
   - Rechazo de código inexistente con mensaje unificado.
   - Rechazo de contraseña incorrecta con verificación BCrypt.
   - Rechazo de cuenta desactivada con mensaje neutro anti-enumeración.
   - Rechazo de solicitudes con parámetros nulos.
2. `CerrarSesionUseCaseTest`:
   - Confirmación exitosa con timestamp.
3. `JwtServiceTest`:
   - Generación y extracción de claims de rol e identidad.
   - Rechazo de tokens manipulados o alterados.
   - Rechazo de tokens expirados.
4. `AuthControllerTest`:
   - Login 200 OK con token de 8 horas y datos públicos sin credenciales sensibles.
   - Login 400 Bad Request por campos vacíos o con espacios.
   - Login 401 Unauthorized por credenciales inválidas.
   - Logout 200 OK cuando el usuario está autenticado.
   - Logout 401 Unauthorized sin encabezado Authorization.
   - **Requisito 8:** Logout 401 Unauthorized cuando el usuario tiene un token formalmente válido pero su cuenta fue desactivada en base de datos.
   - Logout 200 OK cuando el usuario tiene token válido y cuenta activa en base de datos.
5. **Regresión HU-01:**
   - `RegistrarUsuarioUseCaseTest` (4 pruebas) y `UsuarioControllerTest` (4 pruebas) continúan con 100% de éxito.

---

## 11. Criterios de Aceptación Cubiertos

| Criterio | Descripción | Estado | Validación |
| :--- | :--- | :---: | :--- |
| **CA-01** | Inicio de sesión exitoso por rol | **CUMPLIDO** | Valida credenciales y genera token JWT con claims `ADMINISTRADORA` o `TRABAJADORA`. |
| **CA-02** | Credenciales incorrectas o inactiva | **CUMPLIDO** | Retorna 401 Unauthorized con mensaje genérico seguro en español. |
| **CA-03** | Protección de rutas privadas | **CUMPLIDO** | `JwtAuthenticationFilter` valida firma, vigencia y estado activo en persistencia. |
| **CA-05** | Cierre de sesión local | **CUMPLIDO** | Endpoint `POST /api/v1/auth/logout` responde 200 OK confirmando la finalización en servidor. |

---

## 12. Comandos Sugeridos para Registrar los Cambios en Git

Para registrar los cambios en Git organizados de manera atómica por subtarea (sin realizar push ni merge automático):

### Commit 1: Subtarea BIO-29 (Infraestructura de Seguridad y JWT)
```bash
git add backend/pom.xml \
        backend/src/main/resources/application.properties \
        backend/src/test/resources/application.properties \
        backend/src/main/java/com/biofarma/inventario/application/service/TokenProvider.java \
        backend/src/main/java/com/biofarma/inventario/infrastructure/security/JwtService.java \
        backend/src/main/java/com/biofarma/inventario/infrastructure/security/JwtAuthenticationFilter.java \
        backend/src/main/java/com/biofarma/inventario/infrastructure/security/SecurityConfig.java \
        backend/src/test/java/com/biofarma/inventario/infrastructure/security/JwtServiceTest.java

git commit -m "feat(BIO-29): configurar Spring Security, generacion/validacion JWT y filtro de autenticacion"
```

### Commit 2: Subtarea BIO-30 (Servicio y Endpoints de Inicio/Cierre de Sesión)
```bash
git add backend/src/main/java/com/biofarma/inventario/domain/exception/CredencialesInvalidasException.java \
        backend/src/main/java/com/biofarma/inventario/application/dto/LoginRequest.java \
        backend/src/main/java/com/biofarma/inventario/application/dto/LoginResponse.java \
        backend/src/main/java/com/biofarma/inventario/application/dto/LogoutResponse.java \
        backend/src/main/java/com/biofarma/inventario/application/usecase/IniciarSesionUseCase.java \
        backend/src/main/java/com/biofarma/inventario/application/usecase/CerrarSesionUseCase.java \
        backend/src/main/java/com/biofarma/inventario/infrastructure/initialization/AdminPasswordInitializer.java \
        backend/src/main/java/com/biofarma/inventario/presentation/controller/AuthController.java \
        backend/src/main/java/com/biofarma/inventario/presentation/advice/GlobalExceptionHandler.java \
        backend/src/test/java/com/biofarma/inventario/application/usecase/IniciarSesionUseCaseTest.java \
        backend/src/test/java/com/biofarma/inventario/application/usecase/CerrarSesionUseCaseTest.java \
        backend/src/test/java/com/biofarma/inventario/presentation/controller/AuthControllerTest.java \
        database/02_corregir_password_admin.sql \
        BIO-30_IMPLEMENTACION_AUTENTICACION.md

git commit -m "feat(BIO-30): implementar servicio y endpoints REST de inicio y cierre de sesion con verificacion activa"
```
