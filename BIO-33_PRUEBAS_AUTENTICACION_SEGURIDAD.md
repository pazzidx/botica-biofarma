# BIO-33: Ejecución de Pruebas de Autenticación, Cierre de Sesión y Bloqueo por Rol

## 1. Objetivo
Ejecutar, auditar y documentar la suite integral de aseguramiento de calidad (QA) y pruebas de seguridad para la historia de usuario **HU-02 (BIO-6): Inicio y cierre de sesión con control de acceso por rol** en el sistema web de **Botica Biofarma**, validando la interacción real de la arquitectura limpia (Clean Architecture), Spring Security 6 / Spring Boot 4, JWT, base de datos SQL Server (`localhost\ABD_GRUPO2` en puerto 1433) y el cliente React + Vite.

---

## 2. Alcance de las Pruebas
El alcance de esta subtarea abarca:
1. **Conectividad Real de Base de Datos:** Verificación de conexión TCP en puerto 1433 hacia la instancia `localhost\ABD_GRUPO2` sobre la base de datos `DB_BoticaBiofarma`.
2. **Pruebas Funcionales Backend (CP-01 a CP-20):** Ejecución de 20 casos de prueba automatizados mediante peticiones HTTP reales contra el servidor Spring Boot en ejecución (`http://localhost:8080/api/v1`).
3. **Control de Acceso Basado en Roles (RBAC):** Comprobación de autorizaciones estrictas para `ADMINISTRADORA` y restricciones para `TRABAJADORA`.
4. **Resistencia a Tokens Obsoletos o Manipulados:** Comprobación de que la degradación de rol o desactivación de cuenta en la base de datos inhabilite inmediatamente el acceso efectivo.
5. **Pruebas de Frontend (Casos 1 a 22):** Evaluación de la pantalla de login, enrutamiento protegido con `ProtectedRoute`, gestión de sesión en `AuthContext`, persistencia y cierre de sesión.
6. **Pruebas de Regresión y Surefire:** Análisis detallado del conteo de pruebas en Maven Surefire (37 pruebas herméticas) y validación de compilación frontend (`vite build`) y linter (`oxlint`).
7. **Auditoría de Seguridad OWASP:** Ausencia de secretos en repositorio, BCrypt, caducidad de token (8 horas) y análisis de mitigaciones ante XSS/localStorage.

---

## 3. Entorno Tecnológico

| Componente | Tecnología / Versión | Detalle de Configuración |
|---|---|---|
| **Sistema Operativo** | Windows 11 / Windows Server | Shell: PowerShell |
| **Java JDK** | OpenJDK 21.0.8 (JetBrains JBR) | `release 17/21` |
| **Framework Backend** | Spring Boot 4.2.0 | Spring Data JPA, Spring Security, Jackson |
| **Base de Datos** | Microsoft SQL Server (17.0) | Instancia `ABD_GRUPO2`, Puerto TCP `1433`, Base `DB_BoticaBiofarma` |
| **Driver JDBC** | Microsoft JDBC Driver 13.6 for SQL Server | `com.microsoft.sqlserver.jdbc.SQLServerDriver` |
| **Seguridad / Cifrado** | BCrypt (costo 10) + HMAC-SHA256 (JWT) | Clave de 256 bits (`JWT_SECRET`) inyectada por entorno |
| **Frontend** | React 19 + Vite 8.3.8 + React Router 7 | Axios 1.21.1, Lucide React, Oxlint 1.90.0 |
| **Puertos de Red** | Backend: `8080` \| Frontend: `5173` | CORS configurado para `http://localhost:5173` |

---

## 4. Configuración de SQL Server
- **Instancia Seleccionada:** `localhost\ABD_GRUPO2` (Servicio: `MSSQL$ABD_GRUPO2`).
- **Modo de Autenticación:** Mixto (Autenticación de Windows y SQL Server).
- **Habilitación de Red:** Protocolo TCP/IP habilitado en el registro de SQL Server con puerto estático `1433`.
- **Base de Datos Creada:** `DB_BoticaBiofarma`.
- **Tablas Verificadas:**
  - `Roles`: `id_rol`, `nombre` (`ADMINISTRADORA`, `TRABAJADORA`).
  - `Usuarios`: `id_usuario`, `codigo_usuario`, `nombres`, `apellidos`, `password_hash`, `id_rol`, `activo`, `fecha_creacion`.
  - `Productos` y `Lotes` (esquema oficial de Sprint 1 respetado).

---

## 5. Datos de Prueba (Sin Credenciales Sensibles)
Para la ejecución de pruebas reales se prepararon las siguientes cuentas en `DB_BoticaBiofarma`, todas protegidas con BCrypt:

| Código Usuario | Rol | Estado | Propósito de Prueba |
|---|---|---|---|
| `admin` | `ADMINISTRADORA` | Activo (`1`) | Verificación de inicio de sesión de administradora y gestión de usuarios. |
| `c_mendoza` | `TRABAJADORA` | Activo (`1`) | Verificación de inicio de sesión de trabajadora y bloqueo de gestión. |
| `user_inactivo` | `TRABAJADORA` | Inactivo (`0`) | Comprobación de rechazo de autenticación para cuentas deshabilitadas. |
| `user_degradado` | `ADMINISTRADORA` -> `TRABAJADORA` | Activo (`1`) | Verificación de pérdida de permisos administrativos al degradar en BD. |
| `user_bloqueado` | `ADMINISTRADORA` | Activo (`1`) -> Inactivo (`0`) | Verificación de rechazo inmediato con token válido al desactivar en BD. |

---

## 6. Matriz de Casos de Prueba Backend (CP-01 a CP-20)

| ID | Descripción del Caso | Método | Endpoint | HTTP Esperado | HTTP Obtenido | Resultado |
|---|---|---|---|:---:|:---:|:---:|
| **CP-01** | Login exitoso como `ADMINISTRADORA` | POST | `/api/v1/auth/login` | 200 OK | 200 OK | **EXITOSO** |
| **CP-02** | Login exitoso como `TRABAJADORA` | POST | `/api/v1/auth/login` | 200 OK | 200 OK | **EXITOSO** |
| **CP-03** | Login con contraseña incorrecta | POST | `/api/v1/auth/login` | 401 Unauthorized | 401 Unauthorized | **EXITOSO** |
| **CP-04** | Login con código de usuario inexistente | POST | `/api/v1/auth/login` | 401 Unauthorized | 401 Unauthorized | **EXITOSO** |
| **CP-05** | Login con usuario inactivo | POST | `/api/v1/auth/login` | 401 Unauthorized | 401 Unauthorized | **EXITOSO** |
| **CP-06** | Login con campos obligatorios vacíos | POST | `/api/v1/auth/login` | 400 Bad Request | 400 Bad Request | **EXITOSO** |
| **CP-07** | Acceso a gestión de usuarios sin token | GET | `/api/v1/usuarios` | 401 Unauthorized | 401 Unauthorized | **EXITOSO** |
| **CP-08** | Acceso a gestión de usuarios con token inválido/alterado | GET | `/api/v1/usuarios` | 401 Unauthorized | 401 Unauthorized | **EXITOSO** |
| **CP-09** | Acceso con token expirado | GET | `/api/v1/usuarios` | 401 Unauthorized | 401 Unauthorized | **EXITOSO** |
| **CP-10** | Listado de usuarios como `ADMINISTRADORA` | GET | `/api/v1/usuarios` | 200 OK | 200 OK | **EXITOSO** |
| **CP-11** | Registro de usuario como `ADMINISTRADORA` | POST | `/api/v1/usuarios` | 201 Created | 201 Created | **EXITOSO** |
| **CP-12** | Listado de usuarios como `TRABAJADORA` | GET | `/api/v1/usuarios` | 403 Forbidden | 403 Forbidden | **EXITOSO** |
| **CP-13** | Registro de usuario como `TRABAJADORA` | POST | `/api/v1/usuarios` | 403 Forbidden | 403 Forbidden | **EXITOSO** |
| **CP-14** | Intento de acceso a usuario por ID como `TRABAJADORA` | GET | `/api/v1/usuarios/1` | 403 Forbidden | 403 Forbidden | **EXITOSO** |
| **CP-15** | Cierre de sesión como `ADMINISTRADORA` | POST | `/api/v1/auth/logout` | 200 OK | 200 OK | **EXITOSO** |
| **CP-16** | Cierre de sesión como `TRABAJADORA` | POST | `/api/v1/auth/logout` | 200 OK | 200 OK | **EXITOSO** |
| **CP-17** | Cierre de sesión sin autenticación | POST | `/api/v1/auth/logout` | 401 Unauthorized | 401 Unauthorized | **EXITOSO** |
| **CP-18** | Acceso con token emitido previo a desactivar usuario en BD | GET | `/api/v1/usuarios` | 401 Unauthorized | 401 Unauthorized | **EXITOSO** |
| **CP-19** | Acceso admin con token emitido previo a degradar rol a TRABAJADORA | GET | `/api/v1/usuarios` | 403 Forbidden | 403 Forbidden | **EXITOSO** |
| **CP-20** | Auditoría: No exposición de contraseñas, hashes ni secretos | AUDIT | `/api/v1/**` | 0 alertas | 0 alertas | **EXITOSO** |

---

## 7. Resultados Esperados y Obtenidos (Detalle de Casos Críticos)

### CP-01 y CP-02: Autenticación Exitosa
- **Petición:** `POST /api/v1/auth/login` con `{ codigoUsuario, password }`.
- **Respuesta:** `200 OK`.
- **Payload obtenido:**
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.ey... [TOKEN PROTEGIDO]",
    "tipoToken": "Bearer",
    "expiraEnMs": 28800000,
    "idUsuario": 1,
    "codigoUsuario": "admin",
    "nombres": "Propietaria",
    "apellidos": "Biofarma",
    "rol": "ADMINISTRADORA"
  }
  ```
- **Evaluación:** Cumple estrictamente con el contrato. No expone contraseñas ni hashes.

### CP-05: Rechazo de Usuario Inactivo
- **Petición:** `POST /api/v1/auth/login` con `{ codigoUsuario: "user_inactivo", password: "..." }`.
- **Respuesta:** `401 Unauthorized`.
- **Mensaje:** `"Credenciales incorrectas o usuario inactivo."`.
- **Evaluación:** Mensaje neutro que no revela a un atacante si la cuenta existe o fue suspendida.

### CP-18 y CP-19: Validación Dinámica contra Persistencia
- **Escenario CP-18:** El usuario `user_bloqueado` obtiene un token JWT válido. Inmediatamente después, se ejecuta `UPDATE Usuarios SET activo = 0` en SQL Server. Al intentar invocar `/api/v1/usuarios`, `JwtAuthenticationFilter` consulta `UsuarioRepository.findByCodigoUsuario()`, detecta `activo = false`, limpia el contexto y responde `401 Unauthorized`.
- **Escenario CP-19:** El usuario `user_degradado` obtiene un token con reclamo de rol `ADMINISTRADORA`. Se ejecuta `UPDATE Usuarios SET id_rol = 2` (TRABAJADORA) en SQL Server. Al intentar acceder a la gestión de usuarios, el filtro toma el rol real de la base de datos (`ROLE_TRABAJADORA`) y el controlador deniega el acceso con `403 Forbidden`.
- **Evaluación:** Se neutraliza el riesgo de reclamos obsoletos en tokens de larga duración.

---

## 8. Pruebas de Integración y Regresión (Maven Surefire)

### Análisis del Conteo de Pruebas Surefire
En auditorías previas se requería aclarar por qué Maven Surefire reportaba 37 pruebas. Tras inspeccionar los reportes individuales generados en `backend/target/surefire-reports`, se obtuvo el desglose exacto:

| Clase de Prueba | Pruebas Ejecutadas | Fallos | Errores | Omitidas |
|---|:---:|:---:|:---:|:---:|
| `CerrarSesionUseCaseTest` | 1 | 0 | 0 | 0 |
| `IniciarSesionUseCaseTest` | 6 | 0 | 0 | 0 |
| `RegistrarUsuarioUseCaseTest` | 4 | 0 | 0 | 0 |
| `BackendApplicationTests` | 1 | 0 | 0 | 0 |
| `AutorizacionRolesTest` | 6 | 0 | 0 | 0 |
| `JwtServiceTest` | 4 | 0 | 0 | 0 |
| `AuthControllerTest` | 7 | 0 | 0 | 0 |
| `UsuarioControllerTest` | 8 | 0 | 0 | 0 |
| **TOTAL CONSOLIDADO** | **37** | **0** | **0** | **0** |

*Nota aclaratoria:* La documentación preliminar de BIO-31 contenía una errata tipográfica menor (describía 8 pruebas en `AutorizacionRolesTest` y 3 en `JwtServiceTest` = 11); en el código fuente real existen exactamente 6 métodos de prueba en `AutorizacionRolesTest` y 4 métodos en `JwtServiceTest` (10 en conjunto), totalizando exactamente **37 pruebas automáticas**, las cuales pasan al 100% sin fallos.

---

## 9. Pruebas de Frontend (Casos 1 al 22)

| # | Escenario de Prueba Frontend | Mecanismo de Verificación | Resultado |
|:---:|---|---|:---:|
| **1** | Visualización correcta de pantalla de login | `LoginPage.jsx` renderiza inputs, títulos y branding | **Verificado** |
| **2** | Validación de campos obligatorios | Función `validarFormulario()` bloquea submit vacío | **Verificado** |
| **3** | Mostrar y ocultar contraseña | Botón `toggle-password-btn` alterna entre `text` y `password` | **Verificado** |
| **4** | Inicio de sesión como `ADMINISTRADORA` | `authApi.login` conecta a backend y recibe token | **Verificado** |
| **5** | Visualización de opciones administrativas | `AppLayout.jsx` renderiza enlace "Gestión de Usuarios" | **Verificado** |
| **6** | Acceso a gestión de usuarios | Ruta `/usuarios` accesible bajo rol `ADMINISTRADORA` | **Verificado** |
| **7** | Registro de trabajadora desde UI | `UsuariosPage.jsx` envía POST exitoso con token | **Verificado** |
| **8** | Cierre de sesión de `ADMINISTRADORA` | Botón en navbar invoca `logout()` y limpia storage | **Verificado** |
| **9** | Inicio de sesión como `TRABAJADORA` | Login con credenciales de trabajadora exitoso | **Verificado** |
| **10** | Ocultamiento de menú administrativo | `isAdmin = false` oculta el enlace en navbar | **Verificado** |
| **11** | Bloqueo de acceso directo a `/usuarios` | `ProtectedRoute` intercepta URL directa | **Verificado** |
| **12** | Visualización de acceso denegado | Redirección a `/acceso-denegado` (HTTP 403) | **Verificado** |
| **13** | Mensaje de credenciales incorrectas | Captura de 401 en login muestra banner de error | **Verificado** |
| **14** | Persistencia de sesión al recargar | `readInitialSession()` restaura datos de `localStorage` | **Verificado** |
| **15** | Bloqueo de sesión expirada | `isTokenExpired()` limpia sesión y redirige a `/login` | **Verificado** |
| **16** | Limpieza de token al cerrar sesión | `localStorage.removeItem(biofarma_token)` | **Verificado** |
| **17** | Bloqueo de rutas privadas tras logout | Intento de navegación redirige a `/login` | **Verificado** |
| **18** | Botón Atrás del navegador tras logout | Sesión nula; guardia rebota al login | **Verificado** |
| **19** | Manejo de HTTP 401 durante sesión | Interceptor `httpClient` emite `biofarma:unauthorized` | **Verificado** |
| **20** | Manejo de HTTP 403 sin desloguear | Redirección a pantalla 403 preservando sesión | **Verificado** |
| **21** | Manejo de errores de conexión | Captura de error de red con mensaje amigable | **Verificado** |
| **22** | Visualización responsive | Reglas `@media (max-width: ...)` en login y navbar | **Verificado** |

---

## 10. Auditoría de Seguridad OWASP

1. **Gestión de Contraseñas:**
   - Cifradas exclusivamente con algoritmo BCrypt.
   - Prohibición absoluta de almacenamiento de contraseñas en texto plano.
2. **Protección de Clave Secreta (`JWT_SECRET`):**
   - No existe clave predeterminada en el código fuente ni en `application.properties`.
   - Se inyecta estrictamente mediante variable de entorno `JWT_SECRET` (mínimo 256 bits).
   - `JwtService` valida en tiempo de construcción que la clave no sea nula ni menor a 32 caracteres.
3. **Control de Sesión Stateless y CA-05 de Jira:**
   - El sistema opera con tokens JWT sin estado (stateless).
   - Conforme al criterio **CA-05 de Jira**, el cierre de sesión elimina la sesión local en el cliente y redirige al login.
   - **Limitación documentada:** La arquitectura JWT stateless pura no revoca anticipadamente el token emitido en el servidor hasta que expira su vigencia (8 horas). La mitigación implementada consiste en verificar el estado del usuario en la base de datos en cada petición, permitiendo suspender o degradar una cuenta de inmediato si fuera necesario.
4. **Almacenamiento Local (`localStorage`):**
   - Se almacenan únicamente el token JWT y datos de presentación visual (`nombres`, `rol`).
   - Nunca se almacenan contraseñas, hashes ni secretos.
   - Se implementó saneamiento estricto de componentes para prevenir vectores XSS.
5. **Configuración CORS:**
   - Restringido a los orígenes autorizados de desarrollo (`http://localhost:5173`, `http://127.0.0.1:5173`).

---

## 11. Errores Identificados y Corregidos Durante la Auditoría
1. **Protocolo TCP/IP Deshabilitado en SQL Server:**
   - *Causa:* La instancia `ABD_GRUPO2` tenía `TCP Enabled = 0` en el registro de Windows.
   - *Corrección:* Se modificó la clave de registro habilitando TCP en puerto `1433` y se reinició el servicio `MSSQL$ABD_GRUPO2`.
2. **Formato de Puerto en Script de Conexión:**
   - *Causa:* Confusión entre sintaxis JDBC (`localhost:port`) y sintaxis .NET SqlClient (`localhost,port`).
   - *Corrección:* Se estandarizó la conexión .NET a `127.0.0.1,1433` y la conexión JDBC a `jdbc:sqlserver://localhost:1433`.
3. **Manejo de Errores 401 en Interceptor HTTP:**
   - *Causa:* El interceptor de Axios trataba por igual el 401 de login (credenciales erróneas) y el 401 de rutas privadas (token expirado).
   - *Corrección:* Se diferenció el endpoint `/api/v1/auth/login` para mostrar el mensaje de credenciales en el formulario sin forzar eventos de desautenticación global.

---

## 12. Validación de Criterios de Aceptación (Jira)

| Criterio de Aceptación | Casos de Prueba | Resultado Esperado | Resultado Obtenido | Estado |
|---|---|---|---|:---:|
| **CA-01: Inicio de sesión exitoso** | CP-01, CP-02 | Login 200 OK con token y datos públicos | Token y datos recibidos correctamente | **CUMPLIDO** |
| **CA-02: Rechazo de credenciales incorrectas / inactivas** | CP-03, CP-04, CP-05, CP-06 | HTTP 401 / 400 sin revelar detalles internos | Respuestas estandarizadas con mensaje genérico | **CUMPLIDO** |
| **CA-03: Protección de rutas privadas** | CP-07, CP-08, CP-09 | HTTP 401 ante falta o invalidez de token | Peticiones rechazadas por filtro de seguridad | **CUMPLIDO** |
| **CA-04: Restricción de funciones administrativas** | CP-12, CP-13, CP-14, CP-19 | HTTP 403 Forbidden para rol `TRABAJADORA` | Bloqueo efectivo tanto en API como en UI | **CUMPLIDO** |
| **CA-05: Cierre de sesión** | CP-15, CP-16, CP-17 | HTTP 200, limpieza local y redirección a login | Sesión cerrada, token eliminado de storage | **CUMPLIDO** |

---

## 13. Conclusión
La subtarea **BIO-33** ha sido completada satisfactoriamente con evidencias empíricas verificadas contra la base de datos real SQL Server `DB_BoticaBiofarma` en `localhost:1433`. La historia de usuario **HU-02 (BIO-6)** se encuentra **100% implementada, probada y lista para revisión final**.
