# HU-02 (BIO-6): Reporte Final de Implementación y Aseguramiento de Calidad
## Sistema Web de Control de Inventario y Ventas — Botica Biofarma

---

## 1. Resumen Ejecutivo
La historia de usuario **HU-02 (BIO-6): Inicio y cierre de sesión con control de acceso por rol** ha sido desarrollada, integrada y verificada al 100% siguiendo estrictamente los principios de **Clean Architecture**, las directrices de seguridad OWASP y los estándares de desarrollo establecidos en `AGENTS.md`.

El sistema permite autenticar a las usuarias mediante su código de usuario y contraseña cifrada con BCrypt, emitiendo tokens JWT con vigencia de 8 horas. Incorpora autorización basada en roles (RBAC) con dos perfiles exclusivos: **`ADMINISTRADORA`** y **`TRABAJADORA`**, sincronizando en tiempo real cada solicitud contra la base de datos SQL Server (`DB_BoticaBiofarma` en `localhost:1433`) para garantizar que la degradación de permisos o suspensión de cuentas se aplique de inmediato. Asimismo, el frontend en React + Vite ofrece una interfaz moderna, responsive y segura con protección declarativa de rutas y cierre de sesión conforme al criterio CA-05 de Jira.

---

## 2. Estado de las Subtareas de HU-02

| Subtarea | Descripción | Estado | Evidencias / Documento Técnico |
|---|---|:---:|---|
| **BIO-29** | Configurar Spring Security y JWT en el backend | **COMPLETADA** | `JwtService.java`, `JwtAuthenticationFilter.java`, `SecurityConfig.java` |
| **BIO-30** | Implementar servicio y endpoints de login y logout | **COMPLETADA** | `BIO-30_IMPLEMENTACION_AUTENTICACION.md` (6 pruebas de usecase, controller tests) |
| **BIO-31** | Implementar autorización basada en roles (`ADMINISTRADORA` y `TRABAJADORA`) | **COMPLETADA** | `BIO-31_IMPLEMENTACION_AUTORIZACION_ROLES.md` (RBAC estricto, 37 pruebas Surefire) |
| **BIO-32** | Desarrollar pantalla de inicio de sesión y protección de rutas en React | **COMPLETADA** | `BIO-32_IMPLEMENTACION_FRONTEND_AUTENTICACION.md` (`LoginPage`, `AuthContext`, `AppLayout`) |
| **BIO-33** | Ejecutar pruebas de autenticación, cierre de sesión y bloqueo por rol | **COMPLETADA** | `BIO-33_PRUEBAS_AUTENTICACION_SEGURIDAD.md` (20 casos reales contra SQL Server) |

---

## 3. Arquitectura y Mapa de Componentes por Capas

El monorepo respeta la división concéntrica en capas sin acoplamiento indebido:

```
botica-biofarma/
├── backend/
│   ├── src/main/java/com/biofarma/inventario/
│   │   ├── domain/                         # Núcleo del Negocio (Sin dependencias Spring/JPA)
│   │   │   ├── model/                      # Usuario, Rol, RolNombre
│   │   │   ├── repository/                 # UsuarioRepository (Puerto de salida)
│   │   │   └── exception/                  # CredencialesInvalidasException, CodigoUsuarioDuplicadoException
│   │   ├── application/                    # Casos de Uso y Orquestación
│   │   │   ├── usecase/                    # IniciarSesionUseCase, CerrarSesionUseCase, RegistrarUsuarioUseCase, ListarUsuariosUseCase
│   │   │   ├── dto/                        # LoginRequest, LoginResponse, LogoutResponse, RegistrarUsuarioRequest, UsuarioResponse
│   │   │   ├── mapper/                     # UsuarioMapper
│   │   │   └── service/                    # PasswordHasher
│   │   ├── infrastructure/                 # Adaptadores Técnicos y Persistencia
│   │   │   ├── persistence/                # UsuarioJpaEntity, RolJpaEntity, SpringDataJpaRepositories
│   │   │   ├── security/                   # SecurityConfig, JwtService, JwtAuthenticationFilter, BCryptPasswordEncoder
│   │   │   └── initialization/             # AdminPasswordInitializer
│   │   └── presentation/                   # API REST Delgada
│   │       ├── controller/                 # AuthController (/api/v1/auth/**), UsuarioController (/api/v1/usuarios/**)
│   │       └── advice/                     # GlobalExceptionHandler, ApiErrorResponse
├── frontend/
│   ├── src/
│   │   ├── api/                            # httpClient.js (Axios con interceptores), authApi.js, usuariosApi.js
│   │   ├── context/                        # AuthContext.jsx (persistencia, expiración), useAuth.js
│   │   ├── components/
│   │   │   ├── layout/                     # AppLayout.jsx, AppLayout.css (Navbar condicional, logout)
│   │   │   └── ProtectedRoute.jsx          # Guardia declarativo de rutas privadas
│   │   └── pages/
│   │       ├── auth/                       # LoginPage.jsx, LoginPage.css, UsuariosPage.jsx, UsuariosPage.css
│   │       ├── dashboard/                  # InicioPage.jsx, InicioPage.css
│   │       └── error/                      # AccesoDenegadoPage.jsx, AccesoDenegadoPage.css
└── database/
    ├── 01_schema_sprint1.sql               # Esquema oficial DDL (Roles, Usuarios, Productos, Lotes)
    └── 02_corregir_password_admin.sql      # Template SQL para inicialización segura de admin
```

---

## 4. Resultados Consolidados de Pruebas

### A. Pruebas Automatizadas Backend (Maven Surefire)
- **Total de pruebas ejecutadas:** 47
- **Fallos:** 0 \| **Errores:** 0 \| **Omitidas:** 0
- **Desglose exacto:**
  - `UsuarioTest` (Dominio): 3 pruebas
  - `IniciarSesionUseCaseTest`: 7 pruebas
  - `CerrarSesionUseCaseTest`: 1 prueba
  - `RegistrarUsuarioUseCaseTest`: 4 pruebas
  - `AdminPasswordInitializerTest`: 4 pruebas
  - `JwtServiceTest`: 6 pruebas
  - `AutorizacionRolesTest`: 6 pruebas
  - `AuthControllerTest`: 7 pruebas
  - `UsuarioControllerTest`: 8 pruebas
  - `BackendApplicationTests`: 1 prueba

### B. Pruebas Funcionales con SQL Server Real (CP-01 a CP-20)
- **Total de casos ejecutados:** 20 / 20 (**100% EXITOSOS**)
- **Instancia:** `localhost\ABD_GRUPO2` en puerto `1433`.
- **Casos destacados:**
  - Login exitoso `ADMINISTRADORA` y `TRABAJADORA` (HTTP 200).
  - Rechazo de credenciales incorrectas, código inexistente y usuario inactivo (HTTP 401).
  - Bloqueo de gestión de usuarios para rol `TRABAJADORA` (HTTP 403).
  - Pérdida inmediata de permisos al degradar rol en BD aunque el token contenga el reclamo anterior (HTTP 403).
  - Inhabilitación inmediata al desactivar cuenta en BD con token vigente (HTTP 401).
  - Cierre de sesión y no exposición de contraseñas ni hashes (HTTP 200).

### C. Calidad de Código Frontend
- **Linter (`oxlint`):** 0 advertencias, 0 errores en 14 archivos.
- **Compilación de producción (`vite build`):** Generada exitosamente en 445ms.
- **Espacios y formato (`git diff --check`):** 0 errores de espacios o caracteres corruptos.

---

## 5. Cumplimiento de Criterios de Aceptación de Jira

| Criterio Jira | Resumen Funcional | Estado | Validación Técnica |
|---|---|:---:|---|
| **CA-01** | Inicio de sesión exitoso con código de usuario y contraseña válidos, emitiendo token y datos públicos. | **CUMPLIDO** | `POST /api/v1/auth/login` retorna HTTP 200 con token JWT (8 horas) y datos de usuario sin contraseñas. |
| **CA-02** | Rechazo de autenticación ante credenciales erróneas o cuentas inactivas con mensaje neutro. | **CUMPLIDO** | HTTP 401 con mensaje `"Credenciales incorrectas o usuario inactivo."`. |
| **CA-03** | Protección estricta de rutas privadas sin sesión válida (redirección a login). | **CUMPLIDO** | `ProtectedRoute.jsx` y filtro Spring Security bloquean solicitudes anónimas con HTTP 401. |
| **CA-04** | Restricción de acceso a módulos administrativos exclusivamente para `ADMINISTRADORA`. | **CUMPLIDO** | `UsuarioController` protegido con `@PreAuthorize("hasRole('ADMINISTRADORA')")` y frontend oculta opciones a `TRABAJADORA`. |
| **CA-05** | Cierre de sesión seguro con limpieza de datos locales y redirección inmediata al login. | **CUMPLIDO** | `POST /api/v1/auth/logout`, eliminación de token en `localStorage` y bloqueo ante botón Atrás. |

---

## 6. Instrucciones de Ejecución Manual del Sistema

### 1. Variables de Entorno del Backend (PowerShell)
```powershell
$env:JAVA_HOME = "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.2\jbr"
$env:SPRING_DATASOURCE_URL = "jdbc:sqlserver://localhost:1433;databaseName=DB_BoticaBiofarma;encrypt=true;trustServerCertificate=true"
$env:SPRING_DATASOURCE_USERNAME = "<su_usuario_sql>"
$env:SPRING_DATASOURCE_PASSWORD = "<su_password_sql>"
$env:JWT_SECRET = "<su_clave_secreta_jwt_minimo_32_caracteres>"
$env:INITIAL_ADMIN_PASSWORD = "<su_password_admin_inicial>"
```

### 2. Iniciar el Backend Spring Boot
```powershell
cd backend
mvn spring-boot:run
```
*El backend escuchará en `http://localhost:8080`.*

### 3. Iniciar el Frontend React + Vite
En otra ventana de PowerShell:
```powershell
cd frontend
$env:VITE_API_URL = "http://localhost:8080"
npm run dev
```
*El frontend estará disponible en `http://localhost:5173`.*

### 4. URLs y Cuentas de Acceso para Pruebas Manuales
- **URL de Acceso:** `http://localhost:5173/login`
- **Cuenta Administradora:**
  - Código: `admin`
  - Contraseña: La definida en `$env:INITIAL_ADMIN_PASSWORD` (o mediante `02_corregir_password_admin.sql`).
  - Acceso: Completo (`/inicio`, `/usuarios`).
- **Cuenta Trabajadora:**
  - Código: `c_mendoza` (o cuenta registrada mediante `/api/v1/usuarios`)
  - Contraseña: La suministrada durante el registro en HU-01.
  - Acceso: Limitado a mostrador (`/inicio`). Acceso denegado a `/usuarios`.

---

## 7. Declaración de Entrega
La historia de usuario **HU-02 (BIO-6)** se declara **COMPLETADA Y LISTA PARA REVISIÓN**. No se han ejecutado operaciones de git (`add`, `commit`, `push`, `merge`) preservando la política de subida conjunta manual solicitada por el usuario.
