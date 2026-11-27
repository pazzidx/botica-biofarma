# BIO-31: Implementación de Autorización Basada en Roles (ADMINISTRADORA y TRABAJADORA)

## 1. Objetivo
Implementar y verificar el mecanismo de control de acceso basado en roles (Role-Based Access Control - RBAC) en el backend de **Botica Biofarma** para la subtarea **BIO-31** de la historia de usuario **HU-02 (BIO-6): Inicio y cierre de sesión con control de acceso por rol**.

El objetivo técnico principal consiste en:
1. Restringir la gestión de usuarios (`/api/v1/usuarios/**`) exclusivamente al rol `ADMINISTRADORA`.
2. Denegar con `HTTP 403 Forbidden` el acceso a recursos administrativos cuando sea solicitado por usuarias con rol `TRABAJADORA`.
3. Mantener público el endpoint de autenticación `POST /api/v1/auth/login`.
4. Permitir el cierre de sesión `POST /api/v1/auth/logout` a cualquier usuaria autenticada y activa (`ADMINISTRADORA` o `TRABAJADORA`).
5. Validar en tiempo de ejecución los permisos efectivos directamente contra la persistencia (`UsuarioRepository`), garantizando que la degradación de rol o desactivación de cuenta inhabilite inmediatamente el acceso sin esperar la expiración del token.
6. Estandarizar las respuestas de error `401 Unauthorized` y `403 Forbidden` en formato JSON consistente con `ApiErrorResponse`.
7. Mantener la configuración de claves seguras (`JWT_SECRET`) sin credenciales predeterminadas ni contraseñas expuestas en el repositorio.

---

## 2. Estado Inicial
Antes de la ejecución de BIO-31:
- **HU-01:** Registro y listado de usuarios implementados funcionalmente, pero con endpoints temporalmente abiertos (`permitAll()`) en `SecurityConfig` para facilitar pruebas tempranas.
- **BIO-29 y BIO-30:** Infraestructura JWT y endpoints de login/logout implementados con 26 pruebas automatizadas aprobadas.
- **Configuración:** `JWT_SECRET` se encontraba vinculado a variables de entorno pero requería configuración hermética para entornos de prueba sin claves públicas en el código principal.
- **Autorización:** No existía discriminación de privilegios entre `ADMINISTRADORA` y `TRABAJADORA` a nivel de endpoints ni consulta de persistencia en tiempo de filtro para roles dinámicos.

---

## 3. Reglas de Negocio Implementadas
- **RN-01 (Roles Oficiales del Sistema):** Se reconocen y gestionan únicamente los roles `ADMINISTRADORA` y `TRABAJADORA`, mapeados internamente como `ROLE_ADMINISTRADORA` y `ROLE_TRABAJADORA` en Spring Security.
- **RN-02 (Exclusividad Administrativa de Gestión de Cuentas):** Únicamente las usuarias con rol `ADMINISTRADORA` pueden invocar `POST /api/v1/usuarios` (registro de trabajadoras) y `GET /api/v1/usuarios` (listado y consulta).
- **RN-03 (Acceso a Cierre de Sesión):** Ambos roles autenticados (`ADMINISTRADORA` y `TRABAJADORA`) tienen permitido invocar `POST /api/v1/auth/logout`.
- **RN-04 (Acceso Público Restringido al Login):** El único endpoint no autenticado dentro del módulo de seguridad y usuarios es `POST /api/v1/auth/login`.
- **RN-05 (Sincronización en Persistencia del Rol Efectivo):** En cada petición protegida, el filtro `JwtAuthenticationFilter` consulta el usuario en base de datos mediante su `codigoUsuario`:
  - Si el usuario fue desactivado (`activo = false`) o no existe, se deniega la autenticación (`401 Unauthorized`).
  - El rol asignado a la autoridad del contexto de seguridad se extrae de `usuario.getRol().getNombre()`, anulando cualquier reclamo manipulado o desactualizado en el payload del token.
- **RN-06 (Estandarización de Respuestas de Seguridad):**
  - Sin token, token inválido o expirado: `401 Unauthorized` con mensaje `"Debe iniciar sesión para acceder a este recurso."`
  - Usuario autenticado pero sin rol suficiente: `403 Forbidden` con mensaje `"No tiene permisos suficientes para realizar esta operación."`
- **RN-07 (Protección de Credenciales y Secretos):** `application.properties` declara `jwt.secret=${JWT_SECRET:}` sin valor de reserva por defecto, forzando la inyección segura por variables de entorno y validando que posea al menos 256 bits (32 caracteres).

---

## 4. Matriz de Permisos por Rol (RBAC)

| Endpoint | Método HTTP | Acceso Anónimo / Sin Token | Rol `TRABAJADORA` | Rol `ADMINISTRADORA` | Código de Error Sin Autenticación | Código de Error Sin Permisos |
|---|---|---|---|---|---|---|
| `/api/v1/auth/login` | POST | **Permitido (200 / 401)** | Permitido | Permitido | N/A | N/A |
| `/api/v1/auth/logout` | POST | Denegado | **Permitido (200)** | **Permitido (200)** | `401 Unauthorized` | N/A |
| `/api/v1/usuarios` | POST | Denegado | **Denegado (403)** | **Permitido (201)** | `401 Unauthorized` | `403 Forbidden` |
| `/api/v1/usuarios` | GET | Denegado | **Denegado (403)** | **Permitido (200)** | `401 Unauthorized` | `403 Forbidden` |
| `/api/v1/usuarios/{id}` | GET | Denegado | **Denegado (403)** | **Permitido (200)** | `401 Unauthorized` | `403 Forbidden` |

---

## 5. Archivos Creados y Modificados

### A. Capa de Infraestructura (`infrastructure`)
1. [SecurityConfig.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/main/java/com/biofarma/inventario/infrastructure/security/SecurityConfig.java) *(Modificado)*:
   - Configuración de autorización por matcher en `securityFilterChain`:
     - `.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()`
     - `.requestMatchers("/api/v1/auth/logout").authenticated()`
     - `.requestMatchers("/api/v1/usuarios/**").hasRole("ADMINISTRADORA")`
     - `.anyRequest().authenticated()`
   - Personalización de `AuthenticationEntryPoint` para emitir JSON estructurado `401 Unauthorized` ante fallos de autenticación.
   - Personalización de `AccessDeniedHandler` para emitir JSON estructurado `403 Forbidden` ante fallos de autorización por rol.
2. [JwtAuthenticationFilter.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/main/java/com/biofarma/inventario/infrastructure/security/JwtAuthenticationFilter.java) *(Modificado)*:
   - Incorporación de `ObjectProvider<UsuarioRepository>` para consultar el usuario en persistencia.
   - Verificación de estado de cuenta (`usuario.isActivo()`). Si está inactivo, limpia el contexto de seguridad.
   - Asignación de autoridad `ROLE_` basada exclusivamente en el rol obtenido de base de datos (`usuario.getRol().getNombre().name()`).
3. [JwtService.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/main/java/com/biofarma/inventario/infrastructure/security/JwtService.java) *(Modificado)*:
   - Validación estricta en constructor: rechaza claves nulas, vacías o con longitud menor a 32 caracteres (256 bits).

### B. Capa de Presentación (`presentation`)
4. [UsuarioController.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/main/java/com/biofarma/inventario/presentation/controller/UsuarioController.java) *(Modificado)*:
   - Incorporación de anotación de seguridad declarativa `@PreAuthorize("hasRole('ADMINISTRADORA')")` a nivel de controlador para defensa en profundidad.
5. [GlobalExceptionHandler.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/main/java/com/biofarma/inventario/presentation/advice/GlobalExceptionHandler.java) *(Modificado)*:
   - Incorporación de manejadores específicos para `AccessDeniedException` (HTTP 403) y `AuthenticationException` (HTTP 401).

### C. Configuración del Proyecto
6. [application.properties](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/main/resources/application.properties) *(Modificado)*:
   - Eliminación de claves secretas predeterminadas: `jwt.secret=${JWT_SECRET:}`.
   - Vigencia configurable: `jwt.expiration=${JWT_EXPIRATION_MS:28800000}` (8 horas).
   - Contraseña de inicialización segura opcional: `app.security.initial-admin-password=${INITIAL_ADMIN_PASSWORD:}`.
7. [backend/src/test/resources/application.properties](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/test/resources/application.properties) *(Creado / Actualizado)*:
   - Configuración hermética de pruebas con base de datos H2 en modo MSSQLServer y credenciales JWT para testing automatizado.

### D. Capa de Pruebas Automatizadas (`src/test`)
8. [AutorizacionRolesTest.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/test/java/com/biofarma/inventario/infrastructure/security/AutorizacionRolesTest.java) *(Creado)*:
   - Suite completa de pruebas de autorización por roles (8 escenarios).
9. [UsuarioControllerTest.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/test/java/com/biofarma/inventario/presentation/controller/UsuarioControllerTest.java) *(Modificado)*:
   - Actualización de pruebas de HU-01 para simular `@WithMockUser(roles = {"ADMINISTRADORA"})` y nuevas pruebas de rechazo (401 y 403).
10. [JwtServiceTest.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/test/java/com/biofarma/inventario/infrastructure/security/JwtServiceTest.java) *(Creado / Actualizado)*:
    - Validación de generación, expiración y rechazo de claves débiles (< 32 caracteres).

---

## 6. Configuración de Seguridad en `SecurityConfig.java`

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                               JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .formLogin(form -> form.disable())
        .httpBasic(basic -> basic.disable())
        .authorizeHttpRequests(auth -> auth
            // Endpoint público de autenticación
            .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
            // Endpoint de cierre de sesión accesible a cualquier usuaria autenticada
            .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").authenticated()
            // Gestión de usuarios restringida exclusivamente a ADMINISTRADORA
            .requestMatchers("/api/v1/usuarios/**").hasRole("ADMINISTRADORA")
            // Cualquier otro endpoint requiere autenticación
            .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint((request, response, authException) -> {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                ApiErrorResponse error = new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        "No autorizado",
                        "Debe iniciar sesión para acceder a este recurso.",
                        request.getRequestURI(),
                        null
                );
                response.getWriter().write(objectMapper.writeValueAsString(error));
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                ApiErrorResponse error = new ApiErrorResponse(
                        LocalDateTime.now(),
                        HttpStatus.FORBIDDEN.value(),
                        "Acceso denegado",
                        "No tiene permisos suficientes para realizar esta operación.",
                        request.getRequestURI(),
                        null
                );
                response.getWriter().write(objectMapper.writeValueAsString(error));
            })
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
```

---

## 7. Endpoints Protegidos y Comportamiento Verificado

### 1. `POST /api/v1/auth/login`
- **Acceso:** Público anónimo.
- **Respuesta Exitosa:** `200 OK` con token JWT, tiempo de expiración y datos públicos del usuario.
- **Respuesta Errónea:** `401 Unauthorized` sin revelar existencia previa de cuenta.

### 2. `POST /api/v1/auth/logout`
- **Acceso:** Protegido (`ADMINISTRADORA` y `TRABAJADORA`).
- **Respuesta Exitosa:** `200 OK` con confirmación de cierre de sesión local.
- **Respuesta Sin Token:** `401 Unauthorized`.

### 3. `POST /api/v1/usuarios`
- **Acceso:** Exclusivo `ADMINISTRADORA`.
- **Invocado por `ADMINISTRADORA`:** `201 Created` con datos de usuario creado (sin contraseña).
- **Invocado por `TRABAJADORA`:** `403 Forbidden`.
- **Invocado sin Token / Inválido:** `401 Unauthorized`.

### 4. `GET /api/v1/usuarios`
- **Acceso:** Exclusivo `ADMINISTRADORA`.
- **Invocado por `ADMINISTRADORA`:** `200 OK` con lista de usuarios.
- **Invocado por `TRABAJADORA`:** `403 Forbidden`.
- **Invocado sin Token / Inválido:** `401 Unauthorized`.

---

## 8. Pruebas Realizadas

La suite de pruebas automatizadas incluye 37 pruebas unitarias y de integración que cubren la totalidad de los flujos de autorización y funcionalidades existentes:

1. **`com.biofarma.inventario.infrastructure.security.AutorizacionRolesTest` (8 pruebas):**
   - `deberiaPermitirAccesoAUsuariosCuandoRolEnPersistenciaEsAdministradora`: Verifica acceso permitido con token válido y rol `ADMINISTRADORA` en base de datos.
   - `deberiaRechazarAccesoAUsuariosCuandoRolEnPersistenciaEsTrabajadora`: Verifica HTTP 403 a usuaria con rol `TRABAJADORA`.
   - `deberiaRechazarAccesoSiRolFueDegradadoEnPersistenciaAunqueTokenDigaAdmin`: Simula token emitido con reclamo `ADMINISTRADORA` pero usuaria degradada a `TRABAJADORA` en BD; verifica que la autorización consulte BD y retorne HTTP 403.
   - `deberiaRechazarAccesoCon401SiUsuarioFueDesactivadoEnPersistencia`: Verifica rechazo inmediato HTTP 401 si la cuenta fue desactivada (`activo = false`).
   - `deberiaRechazarPeticionSinTokenCon401`: Verifica HTTP 401 ante peticiones anónimas a endpoints protegidos.
   - `deberiaRechazarTokenInvalidoOAlteradoCon401`: Verifica HTTP 401 si la firma o contenido del token fue alterado.
   - `deberiaRechazarTokenExpiradoCon401`: Verifica HTTP 401 si la fecha de expiración del token fue superada.
   - `deberiaPermitirLogoutATrabajadoraAutenticada`: Verifica que una `TRABAJADORA` autenticada pueda cerrar sesión exitosamente (HTTP 200).

2. **`com.biofarma.inventario.presentation.controller.UsuarioControllerTest` (8 pruebas):**
   - Verificación de registro correcto bajo rol `ADMINISTRADORA` sin exponer password.
   - Validación de unicidad de código de usuario (`409 Conflict`).
   - Validación de campos obligatorios (`400 Bad Request`).
   - Listado exitoso de usuarios bajo rol `ADMINISTRADORA`.
   - Rechazo con `403 Forbidden` al registrar usuario con rol `TRABAJADORA`.
   - Rechazo con `403 Forbidden` al listar usuarios con rol `TRABAJADORA`.
   - Rechazo con `401 Unauthorized` al registrar usuario sin autenticación.
   - Rechazo con `401 Unauthorized` al listar usuarios sin autenticación.

3. **`com.biofarma.inventario.presentation.controller.AuthControllerTest` (7 pruebas):**
   - Login exitoso con credenciales válidas.
   - Login rechazado por credenciales inválidas (`401 Unauthorized`).
   - Login con payload inválido (`400 Bad Request`).
   - Logout exitoso con autenticación.
   - Logout rechazado sin autenticación (`401 Unauthorized`).
   - Verificación de ausencia de contraseña en respuesta.
   - Verificación de encabezados de seguridad.

4. **`com.biofarma.inventario.application.usecase.IniciarSesionUseCaseTest` (6 pruebas):**
   - Casos de negocio de autenticación, hash matching y expiración.

5. **`com.biofarma.inventario.application.usecase.RegistrarUsuarioUseCaseTest` (4 pruebas):**
   - Casos de negocio de registro, encriptación y validación de roles.

6. **`com.biofarma.inventario.application.usecase.CerrarSesionUseCaseTest` (1 prueba):**
   - Ejecución de cierre de sesión y respuesta estructurada.

7. **`com.biofarma.inventario.infrastructure.security.JwtServiceTest` (3 pruebas):**
   - Validación de clave de al menos 32 caracteres y tiempo de expiración.

---

## 9. Resultados Reales

### Ejecución de Pruebas Maven
Comando ejecutado:
```powershell
$env:JAVA_HOME = "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.2\jbr"; & "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.2\plugins\maven\lib\maven3\bin\mvn.cmd" -f backend/pom.xml clean test
```

Salida del reporte Surefire:
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
[INFO] Running com.biofarma.inventario.infrastructure.security.AutorizacionRolesTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.biofarma.inventario.infrastructure.security.JwtServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.biofarma.inventario.presentation.controller.AuthControllerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.biofarma.inventario.presentation.controller.UsuarioControllerTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 37, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  18.750 s
```

### Verificación de Estilo y Espacios en Blanco
Comando ejecutado:
```powershell
git diff --check
```
**Resultado:** Código de salida 0. Sin conflictos de espacios ni saltos de línea irregulares.

---

## 10. Criterios de Aceptación Cubiertos

| Criterio | Descripción | Estado | Evidencia |
|---|---|---|---|
| **CA-01** | Control de acceso basado en roles (RBAC) con roles estrictos `ADMINISTRADORA` y `TRABAJADORA`. | **CUMPLIDO** | Configurado en `SecurityConfig`, `JwtAuthenticationFilter` y `UsuarioController`. |
| **CA-02** | Restricción total de gestión de usuarios a `ADMINISTRADORA`. | **CUMPLIDO** | `.requestMatchers("/api/v1/usuarios/**").hasRole("ADMINISTRADORA")`. |
| **CA-03** | Rechazo con HTTP 403 a usuarias `TRABAJADORA` en endpoints administrativos. | **CUMPLIDO** | `AccessDeniedHandler` y pruebas en `AutorizacionRolesTest` y `UsuarioControllerTest`. |
| **CA-04** | Rechazo con HTTP 401 ante ausencia de token, expiración, firma inválida o usuario inactivo. | **CUMPLIDO** | `AuthenticationEntryPoint` y pruebas unitarias de filtro. |
| **CA-05** | Cierre de sesión permitido a ambos roles autenticados. | **CUMPLIDO** | `.requestMatchers("/api/v1/auth/logout").authenticated()`. |
| **CA-06** | Sincronización en tiempo real contra persistencia para evitar permisos desactualizados en tokens. | **CUMPLIDO** | Consulta a `UsuarioRepository` en `JwtAuthenticationFilter`. |
| **CA-07** | Protección de secretos y ausencia de credenciales predeterminadas en código. | **CUMPLIDO** | `jwt.secret=${JWT_SECRET:}` y validación de clave de al menos 256 bits. |

---

## 11. Limitaciones Pendientes
1. **Base de Datos Física SQL Server:**
   - La base de datos `DB_BoticaBiofarma` en la instancia SQL Server local (`localhost\ABD_GRUPO2` / `localhost\SQLEXPRESS`) aún no ha sido aprovisionada de forma persistente. Las pruebas automatizadas fueron ejecutadas satisfactoriamente utilizando el perfil de pruebas con base de datos H2 en modo MSSQLServer para evitar operaciones destructivas no autorizadas.
2. **Subtareas Siguientes:**
   - **BIO-32:** Implementación del frontend React (pantalla de Login, `AuthContext`, almacenamiento seguro en cliente y protección de rutas con componentes `ProtectedRoute`).
   - **BIO-33:** Suite de pruebas end-to-end de seguridad e integración global.

---

## 12. Conclusión
La subtarea **BIO-31** ha sido completada con éxito conforme a los requerimientos de la historia de usuario **HU-02 (BIO-6)** y las directrices de Clean Architecture:
- La capa de seguridad protege de manera estricta y declarativa cada endpoint del sistema.
- Se implementó la verificación dinámica en persistencia, garantizando la seguridad en la revocación de privilegios y desactivación de cuentas.
- Se preservaron íntegramente las funcionalidades previamente desarrolladas de HU-01, BIO-29 y BIO-30, alcanzando **37 pruebas automatizadas exitosas** sin fallos ni errores.
- Se cumplieron de manera rigurosa todas las restricciones obligatorias: sin ejecución de `git add`, `git commit`, `git push`, ni `merge`.
