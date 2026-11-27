# HU-02 (BIO-6): DOCUMENTACIÓN DE APLICACIÓN DE TEST-DRIVEN DEVELOPMENT (TDD)
## Sistema Web de Gestión de Inventario y Ventas — Botica Biofarma

---

## 1. OBJETIVO Y FUNDAMENTOS DE TDD EN EXTREME PROGRAMMING (XP)

### 1.1. Contexto Metodológico
En el marco de la metodología ágil **Extreme Programming (XP)**, el **Desarrollo Guiado por Pruebas (Test-Driven Development - TDD)** es una práctica medular de ingeniería de software orientada a producir código simple, limpio, modular y respaldado por una red de seguridad automatizada de regresión continua.

TDD no es primordialmente una técnica de aseguramiento de calidad (QA), sino una **disciplina de diseño emergente** que invierte el flujo tradicional de desarrollo:
1. **Fase RED (Rojo):** Se diseña la especificación y comportamiento esperado escribiendo primero una prueba automatizada pequeña y representativa. Al ejecutarla, la prueba **debe fallar** obligatoriamente, demostrando que la funcionalidad o regla de negocio aún no existe o no satisface el contrato.
2. **Fase GREEN (Verde):** Se implementa la cantidad mínima y estrictamente necesaria de código de producción para que la prueba pase exitosamente, sin sobreingeniería ni anticipación innecesaria.
3. **Fase REFACTOR (Refactorización):** Se reestructura el código para eliminar duplicidades, mejorar la legibilidad, abstraer responsabilidades y garantizar el cumplimiento estricto de **Clean Architecture** y principios **SOLID**, manteniendo las pruebas ejecutándose en verde de forma ininterrumpida.

```
       ┌────────────────────────┐
       │   1. Escribir Prueba   │
       └───────────┬────────────┘
                   │
                   ▼
       ┌────────────────────────┐
       │     FASE RED (🔴)      │ ──► La prueba falla con evidencia comprobable
       └───────────┬────────────┘
                   │
                   ▼
       ┌────────────────────────┐
       │   Implementar Mínimo   │
       └───────────┬────────────┘
                   │
                   ▼
       ┌────────────────────────┐
       │    FASE GREEN (🟢)     │ ──► La prueba pasa (0 fallos, 0 errores)
       └───────────┬────────────┘
                   │
                   ▼
       ┌────────────────────────┐
       │  FASE REFACTOR (🔵)    │ ──► Limpieza, diseño y arquitectura sin romper tests
       └────────────────────────┘
```

### 1.2. Alcance en la Historia de Usuario HU-02 (BIO-6)
- **Historia de Usuario:** HU-02 (BIO-6) — *Inicio y cierre de sesión con control de acceso por rol*.
- **Subtareas involucradas:** BIO-29 (Seguridad JWT), BIO-30 (Autenticación), BIO-31 (Autorización RBAC), BIO-32 (Frontend React) y BIO-33 (Pruebas integrales).
- **Meta del presente informe:** Auditar la suite de pruebas existente, transparentar qué pruebas fueron desarrolladas ex-post (Test-Last) vs. Test-First, y aplicar rigurosamente el ciclo RED -> GREEN -> REFACTOR sobre 3 nuevas reglas de negocio críticas de seguridad y dominio.

---

## 2. AUDITORÍA DEL ESTADO PREVIO VS. TDD

### 2.1. Inventario y Tipología de Pruebas Previas
Con anterioridad a esta sesión, el sistema contaba con **37 pruebas automatizadas** en el backend y **20 casos de prueba funcionales** end-to-end ejecutados contra SQL Server.

#### Desglose de las 37 Pruebas Automatizadas Iniciales
| Capa Clean Architecture | Clase de Prueba | Tipo de Prueba | N° Tests | Enfoque Inicial |
| :--- | :--- | :--- | :---: | :--- |
| **Dominio** (`domain`) | *(Ninguna)* | Unitaria de Dominio | 0 | **Deuda Técnica:** Reglas de negocio no probadas de forma aislada en POJOs |
| **Aplicación** (`application`) | `IniciarSesionUseCaseTest` | Unitaria con Mockito | 6 | Test-Last (pruebas de casos de uso tras la codificación) |
| **Aplicación** (`application`) | `CerrarSesionUseCaseTest` | Unitaria con Mockito | 1 | Test-Last |
| **Aplicación** (`application`) | `RegistrarUsuarioUseCaseTest` | Unitaria con Mockito | 4 | Test-Last (proveniente de HU-01) |
| **Infraestructura** (`infrastructure`) | `JwtServiceTest` | Seguridad / Unitaria | 4 | Test-Last (validación de claims y expiración) |
| **Infraestructura / Seguridad** | `AutorizacionRolesTest` | Integración Spring Security | 6 | Test-Last (RBAC `@SpringBootTest` con `@WithMockUser`) |
| **Presentación** (`presentation`) | `AuthControllerTest` | Integración MockMvc REST | 7 | Test-Last (Endpoints `/auth/login`, `/auth/logout`) |
| **Presentación** (`presentation`) | `UsuarioControllerTest` | Integración MockMvc REST | 8 | Test-Last (Endpoints `/usuarios/**` con seguridad RBAC) |
| **Infraestructura** | `BackendApplicationTests` | Context Loads | 1 | Smoke Test de arranque Spring Boot |
| **TOTAL INICIAL** | **8 Clases** | — | **37** | **100% pasando (0 fallos, 0 errores)** |

#### 20 Casos de Prueba Funcionales (BIO-33)
- Ejecutados en entorno integrado real con SQL Server (`localhost:1433`, base de datos `DB_BoticaBiofarma`).
- Tipología: **Pruebas de Sistema / End-to-End (E2E)** de caja negra y caja gris que validan la interoperabilidad entre Frontend React, Backend Spring Boot y Base de Datos SQL Server.

### 2.2. Diagnóstico de Transparencia: Test-Last vs. Test-First (TDD Genuino)
- **Hallazgo Crítico:** Las 37 pruebas automatizadas preexistentes fueron escritas **posteriormente o en paralelo a la codificación** de las clases de producción (práctica comúnmente llamada *Test-After* o *Test-Last*). Si bien otorgaban una cobertura de regresión adecuada, no guiaron el diseño arquitectónico inicial.
- **Vacíos Detectados que Requirieron TDD:**
  1. **Ausencia de Pruebas Unitarias de Dominio Puro:** La capa `domain.model.Usuario` dependía de que los casos de uso validaran flags booleanos dispersos en lugar de encapsular su propia invariante de negocio (`puedeAutenticarse()`).
  2. **Validación Temprana y Prevención de Consultas Inútiles (Fast-Fail):** El caso de uso permitía que credenciales en blanco o con solo espacios (`"   "`) llegaran a consultar el repositorio de persistencia, consumiendo recursos de base de datos innecesariamente.
  3. **Contrato de Extracción de Expiración JWT:** El componente de infraestructura `JwtService` no exponía un método público seguro y documentado para extraer la fecha exacta de expiración (`Date`), limitando la capacidad de monitoreo de vigencia de sesión.

---

## 3. CICLOS TDD EJECUTADOS (EVIDENCIAS REALES)

A continuación se documentan los 3 ciclos completos de TDD desarrollados bajo el rigor de Extreme Programming.

---

### 3.1. CICLO 1: Encapsulamiento del Comportamiento de Autenticación en la Entidad de Dominio (`Usuario`)

#### A. Regla de Negocio Asociada (RN-AUTH-01)
> *La entidad de dominio `Usuario` debe encapsular la regla que determina si una cuenta está apta para iniciar sesión (`puedeAutenticarse()`). Un usuario solo puede autenticarse si se encuentra en estado activo (`activo == true`), posee un hash de contraseña no nulo y dicho hash no está en blanco. Si está inactivo o carece de credencial válida, debe retornar `false` sin delegar esa lógica al caso de uso ni a la base de datos.*

#### B. Fase RED (Escribir la prueba primero y verificar el fallo)
Se creó la clase de prueba unitaria pura [UsuarioTest.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/test/java/com/biofarma/inventario/domain/model/UsuarioTest.java) en la capa de dominio:

```java
package com.biofarma.inventario.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    private final Rol rolAdmin = new Rol(1, RolNombre.ADMINISTRADORA);

    @Test
    @DisplayName("Debería retornar true en puedeAutenticarse cuando el usuario está activo y tiene passwordHash válido")
    void deberiaPermitirAutenticacionCuandoUsuarioActivoYConPassword() {
        Usuario usuario = Usuario.reconstruir(
                1, "admin", "Admin", "Biofarma", "$2a$10$hashValido", rolAdmin, true, LocalDateTime.now()
        );
        assertThat(usuario.puedeAutenticarse()).isTrue();
    }

    @Test
    @DisplayName("Debería retornar false en puedeAutenticarse cuando el usuario está inactivo")
    void deberiaRechazarAutenticacionCuandoUsuarioEstaInactivo() {
        Usuario usuario = Usuario.reconstruir(
                2, "vendedora", "Ana", "Torres", "$2a$10$hashValido", rolAdmin, false, LocalDateTime.now()
        );
        assertThat(usuario.puedeAutenticarse()).isFalse();
    }

    @Test
    @DisplayName("Debería retornar false en puedeAutenticarse cuando el passwordHash es nulo o vacío")
    void deberiaRechazarAutenticacionCuandoPasswordHashEsNuloOVacio() {
        Usuario usuarioSinPassword = Usuario.reconstruir(
                3, "usuarioSinClave", "Juan", "Perez", null, rolAdmin, true, LocalDateTime.now()
        );
        Usuario usuarioConPasswordEnBlanco = Usuario.reconstruir(
                4, "usuarioBlanco", "Carlos", "Mora", "   ", rolAdmin, true, LocalDateTime.now()
        );
        assertThat(usuarioSinPassword.puedeAutenticarse()).isFalse();
        assertThat(usuarioConPasswordEnBlanco.puedeAutenticarse()).isFalse();
    }
}
```

**Salida Real del Fallo en Maven (Fase RED):**
```
[INFO] --- compiler:3.16.0:testCompile (default-testCompile) @ inventario ---
[INFO] Compiling 9 source files with javac [debug parameters release 17] to target\test-classes
[INFO] -------------------------------------------------------------
[ERROR] COMPILATION ERROR :
[INFO] -------------------------------------------------------------
[ERROR] /C:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/test/java/com/biofarma/inventario/domain/model/UsuarioTest.java:[22,35] cannot find symbol
  symbol:   method puedeAutenticarse()
  location: variable usuario of type com.biofarma.inventario.domain.model.Usuario
[ERROR] /C:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/test/java/com/biofarma/inventario/domain/model/UsuarioTest.java:[31,35] cannot find symbol
  symbol:   method puedeAutenticarse()
  location: variable usuario of type com.biofarma.inventario.domain.model.Usuario
[ERROR] /C:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/test/java/com/biofarma/inventario/domain/model/UsuarioTest.java:[42,45] cannot find symbol
  symbol:   method puedeAutenticarse()
  location: variable usuarioSinPassword of type com.biofarma.inventario.domain.model.Usuario
[ERROR] /C:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/test/java/com/biofarma/inventario/domain/model/UsuarioTest.java:[43,51] cannot find symbol
  symbol:   method puedeAutenticarse()
  location: variable usuarioConPasswordEnBlanco of type com.biofarma.inventario.domain.model.Usuario
[INFO] 4 errors
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
```

#### C. Fase GREEN (Implementación mínima en producción)
Se añadió el método de dominio correspondiente en [Usuario.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/main/java/com/biofarma/inventario/domain/model/Usuario.java):

```java
    /**
     * Regla de dominio: Evalúa si el usuario cumple las condiciones de seguridad
     * necesarias para iniciar sesión en el sistema.
     *
     * @return true si la cuenta está activa y tiene una credencial configurada.
     */
    public boolean puedeAutenticarse() {
        return this.activo && this.passwordHash != null && !this.passwordHash.isBlank();
    }
```

**Salida Real del Éxito en Maven (Fase GREEN):**
```
[INFO] --- surefire:3.6.1:test (default-test) @ inventario ---
[INFO] Running com.biofarma.inventario.domain.model.UsuarioTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.193 s -- in com.biofarma.inventario.domain.model.UsuarioTest
[INFO]
[INFO] Results:
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

#### D. Fase REFACTOR (Mejora estructural y Clean Architecture)
En [IniciarSesionUseCase.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/main/java/com/biofarma/inventario/application/usecase/IniciarSesionUseCase.java), se reemplazó la validación dispersa `if (!usuario.isActivo())` por la invocación del método de dominio `if (!usuario.puedeAutenticarse())`, delegando la regla a quien le corresponde (Dominio) y simplificando el Caso de Uso (Aplicación).

---

### 3.2. CICLO 2: Validación Temprana (Fast-Fail) de Credenciales en Blanco sin Consultar Repositorio

#### A. Regla de Negocio Asociada (RN-AUTH-02)
> *La solicitud de autenticación (`LoginRequest`) debe validarse inmediatamente al ingresar al caso de uso: si el código de usuario o la contraseña están vacíos o compuestos únicamente por espacios en blanco (`isBlank()`), el sistema debe lanzar `CredencialesInvalidasException` de inmediato, sin ejecutar ninguna consulta a base de datos (`usuarioRepository.findByCodigoUsuario`) ni gastar ciclos de CPU en el hasher de contraseñas.*

#### B. Fase RED (Escribir la prueba primero y verificar el fallo)
Se añadió el caso de prueba en [IniciarSesionUseCaseTest.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/test/java/com/biofarma/inventario/application/usecase/IniciarSesionUseCaseTest.java):

```java
    @Test
    @DisplayName("Debería lanzar CredencialesInvalidasException y no consultar repositorio cuando el código o password están en blanco")
    void deberiaLanzarExcepcionYNoConsultarRepositorioCuandoCredencialesEstanEnBlanco() {
        // Arrange
        LoginRequest requestCodigoBlanco = new LoginRequest("   ", "password123");
        LoginRequest requestPasswordBlanco = new LoginRequest("admin", "   ");
        LoginRequest requestAmbosBlanco = new LoginRequest("", "");

        // Act & Assert - Código en blanco
        assertThatThrownBy(() -> useCase.ejecutar(requestCodigoBlanco))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales inválidas.");

        // Act & Assert - Password en blanco
        assertThatThrownBy(() -> useCase.ejecutar(requestPasswordBlanco))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales inválidas.");

        // Act & Assert - Ambos en blanco
        assertThatThrownBy(() -> useCase.ejecutar(requestAmbosBlanco))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Credenciales inválidas.");

        verify(usuarioRepository, never()).findByCodigoUsuario(any());
        verify(passwordHasher, never()).matches(any(), any());
        verify(tokenProvider, never()).generarToken(any());
    }
```

**Salida Real del Fallo en Maven (Fase RED):**
```
[ERROR] Tests run: 7, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 1.444 s <<< FAILURE! -- in com.biofarma.inventario.application.usecase.IniciarSesionUseCaseTest
[ERROR] com.biofarma.inventario.application.usecase.IniciarSesionUseCaseTest.deberiaLanzarExcepcionYNoConsultarRepositorioCuandoCredencialesEstanEnBlanco -- Time elapsed: 0.042 s <<< FAILURE!
org.mockito.exceptions.verification.NeverWantedButInvoked:

usuarioRepository.findByCodigoUsuario(<any>);
Never wanted here:
-> at com.biofarma.inventario.application.usecase.IniciarSesionUseCaseTest.deberiaLanzarExcepcionYNoConsultarRepositorioCuandoCredencialesEstanEnBlanco(IniciarSesionUseCaseTest.java:221)
But invoked here:
-> at com.biofarma.inventario.application.usecase.IniciarSesionUseCase.ejecutar(IniciarSesionUseCase.java:41) with arguments: []
-> at com.biofarma.inventario.application.usecase.IniciarSesionUseCase.ejecutar(IniciarSesionUseCase.java:41) with arguments: [admin]
-> at com.biofarma.inventario.application.usecase.IniciarSesionUseCase.ejecutar(IniciarSesionUseCase.java:41) with arguments: []

	at com.biofarma.inventario.application.usecase.IniciarSesionUseCaseTest.deberiaLanzarExcepcionYNoConsultarRepositorioCuandoCredencialesEstanEnBlanco(IniciarSesionUseCaseTest.java:221)
```

#### C. Fase GREEN (Implementación mínima en producción)
Se incorporó el control en [IniciarSesionUseCase.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/main/java/com/biofarma/inventario/application/usecase/IniciarSesionUseCase.java):

```java
    public LoginResponse ejecutar(LoginRequest request) {
        if (request == null || request.codigoUsuario() == null || request.password() == null) {
            throw new CredencialesInvalidasException(MENSAJE_ERROR_CREDENCIALES);
        }

        if (request.codigoUsuario().isBlank() || request.password().isBlank()) {
            throw new CredencialesInvalidasException(MENSAJE_ERROR_CREDENCIALES);
        }
        ...
```

**Salida Real del Éxito en Maven (Fase GREEN):**
```
[INFO] Running com.biofarma.inventario.application.usecase.IniciarSesionUseCaseTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.396 s -- in com.biofarma.inventario.application.usecase.IniciarSesionUseCaseTest
[INFO]
[INFO] Results:
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

#### D. Fase REFACTOR (Mejora estructural y legibilidad)
Se extrajo la condición en un método privado auxiliar con nombre revelador `esSolicitudInvalida(LoginRequest request)`:

```java
    public LoginResponse ejecutar(LoginRequest request) {
        if (esSolicitudInvalida(request)) {
            throw new CredencialesInvalidasException(MENSAJE_ERROR_CREDENCIALES);
        }

        Usuario usuario = usuarioRepository.findByCodigoUsuario(request.codigoUsuario().trim())
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_ERROR_CREDENCIALES));

        if (!usuario.puedeAutenticarse()) {
            throw new CredencialesInvalidasException(MENSAJE_ERROR_CREDENCIALES);
        }
        ...
    }

    private boolean esSolicitudInvalida(LoginRequest request) {
        return request == null
                || request.codigoUsuario() == null
                || request.codigoUsuario().isBlank()
                || request.password() == null
                || request.password().isBlank();
    }
```
Re-ejecución tras refactor: `Tests run: 7, Failures: 0, Errors: 0, Time elapsed: 1.359 s (BUILD SUCCESS)`.

---

### 3.3. CICLO 3: Extracción Segura de la Fecha de Expiración del Token en `JwtService`

#### A. Regla de Negocio Asociada (RN-AUTH-03)
> *El servicio de tokens `JwtService` debe proporcionar un método público `extraerFechaExpiracion(String token)` que devuelva la fecha exacta (`Date`) contenida en los claims del token válido. Si el token es nulo o vacío debe lanzar `IllegalArgumentException`, y si está corrupto, alterado o inválido debe propagar la excepción criptográfica `JwtException` sin retornar fechas erróneas.*

#### B. Fase RED (Escribir la prueba primero y verificar el fallo)
Se agregaron 2 pruebas unitarias en [JwtServiceTest.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/test/java/com/biofarma/inventario/infrastructure/security/JwtServiceTest.java):

```java
    @Test
    @DisplayName("Debería extraer fecha de expiración correctamente desde token válido")
    void deberiaExtraerFechaExpiracionCorrectamenteDesdeTokenValido() {
        // Arrange
        Rol rol = new Rol(1, RolNombre.ADMINISTRADORA);
        Usuario usuario = Usuario.reconstruir(
                1, "admin", "Propietaria", "Biofarma", "$2a$10$hashed", rol, true, LocalDateTime.now()
        );
        long antesGenerar = System.currentTimeMillis();

        // Act
        String token = jwtService.generarToken(usuario);
        java.util.Date fechaExpiracion = jwtService.extraerFechaExpiracion(token);

        // Assert
        assertThat(fechaExpiracion).isNotNull();
        assertThat(fechaExpiracion.getTime()).isGreaterThan(antesGenerar);
        assertThat(fechaExpiracion.getTime()).isLessThanOrEqualTo(System.currentTimeMillis() + expirationMs + 1000L);
    }

    @Test
    @DisplayName("Debería fallar al extraer fecha de expiración de token inválido o corrupto")
    void deberiaFallarAlExtraerFechaExpiracionDeTokenInvalido() {
        assertThatThrownBy(() -> jwtService.extraerFechaExpiracion("token.falso.corrupto"))
                .isInstanceOf(io.jsonwebtoken.JwtException.class);

        assertThatThrownBy(() -> jwtService.extraerFechaExpiracion(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
```

**Salida Real del Fallo en Maven (Fase RED):**
```
[INFO] Running com.biofarma.inventario.infrastructure.security.JwtServiceTest
[ERROR] Tests run: 6, Failures: 0, Errors: 6, Skipped: 0, Time elapsed: 0.077 s <<< FAILURE! -- in com.biofarma.inventario.infrastructure.security.JwtServiceTest
[ERROR] com.biofarma.inventario.infrastructure.security.JwtServiceTest.deberiaExtraerFechaExpiracionCorrectamenteDesdeTokenValido -- Time elapsed: 0.022 s <<< ERROR!
java.lang.Error:
Unresolved compilation problems:
	The method extraerFechaExpiracion(String) is undefined for the type JwtService
	The method extraerFechaExpiracion(String) is undefined for the type JwtService
	The method extraerFechaExpiracion(null) is undefined for the type JwtService

	at com.biofarma.inventario.infrastructure.security.JwtServiceTest.<init>(JwtServiceTest.java:115)
[INFO] BUILD FAILURE
```

#### C. Fase GREEN (Implementación mínima en producción)
Se implementó el método en [JwtService.java](file:///c:/Users/Erick/Desktop/Botica_Miofarma/botica-biofarma/backend/src/main/java/com/biofarma/inventario/infrastructure/security/JwtService.java):

```java
    public Date extraerFechaExpiracion(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token no puede ser nulo ni vacío.");
        }
        return extraerClaims(token).getExpiration();
    }
```

**Salida Real del Éxito en Maven (Fase GREEN):**
```
[INFO] Running com.biofarma.inventario.infrastructure.security.JwtServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.830 s -- in com.biofarma.inventario.infrastructure.security.JwtServiceTest
[INFO]
[INFO] Results:
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

#### D. Fase REFACTOR (Documentación y tipado estricto)
Se documentó formalmente el contrato del método mediante JavaDoc detallando las excepciones lanzadas y los parámetros recibidos, consolidando la claridad de la capa de infraestructura.

Re-ejecución tras refactor: `Tests run: 6, Failures: 0, Errors: 0, Time elapsed: 0.778 s (BUILD SUCCESS)`.

---

## 4. MATRIZ DE COBERTURA Y TIPOLOGÍA DE PRUEBAS ACTUALIZADA

Con la culminación de los 3 ciclos TDD y la incorporación de las pruebas unitarias del inicializador de seguridad, el sistema cuenta con un total de **47 pruebas automatizadas unitarias y de integración**, además de los **20 casos de prueba funcionales**.

### 4.1. Matriz Consolidada de Pruebas Automatizadas (Backend)
| Capa Arquitectónica | Clase / Módulo | N° Tests | Tipología | Descripción del Alcance Probado |
| :--- | :--- | :---: | :--- | :--- |
| **Dominio** | `UsuarioTest` | **3** | Unitaria Pura | Encapsulamiento de `puedeAutenticarse()` con cuentas activas, inactivas y contraseñas vacías. |
| **Aplicación** | `IniciarSesionUseCaseTest` | **7** | Unitaria (Mockito) | Autenticación exitosa (Admin/Trabajadora), fast-fail por nulos y espacios, usuario inactivo, contraseña errónea, usuario inexistente. |
| **Aplicación** | `CerrarSesionUseCaseTest` | **1** | Unitaria (Mockito) | Retorno de mensaje estándar de revocación local de sesión. |
| **Aplicación** | `RegistrarUsuarioUseCaseTest`| **4** | Unitaria (Mockito) | Registro exitoso, duplicidad de código, rol inexistente, contraseñas no válidas. |
| **Seguridad / Infra.** | `AdminPasswordInitializerTest` | **4** | Unitaria (Mockito) | Validación de auto-inicialización: omitir por password en blanco, actualizar hash inválido, conservar hash válido, omitir sin admin. |
| **Seguridad / Infra.** | `JwtServiceTest` | **6** | Seguridad / Unitaria | Generación de claims, validación de firmas, token manipulado, expiración forzada, clave débil, extracción de fecha de expiración. |
| **Seguridad / Infra.** | `AutorizacionRolesTest` | **6** | Integración Spring Sec. | Restricción RBAC: `@PreAuthorize` en `/usuarios/**`, rechazo HTTP 403 a trabajadoras, aprobación a administradoras, 401 a anónimos. |
| **Presentación** | `AuthControllerTest` | **7** | Integración MockMvc | Serialización JSON, códigos HTTP 200, 400 y 401 en `/auth/login` y `/auth/logout`. |
| **Presentación** | `UsuarioControllerTest` | **8** | Integración MockMvc | Respuestas HTTP y validaciones en endpoints CRUD de usuarios con tokens simulados. |
| **Contexto** | `BackendApplicationTests` | **1** | Integración Spring | Inicialización de beans, carga del ApplicationContext y repositorios JPA en H2 aislado. |
| **TOTAL GENERAL** | **10 Clases** | **47** | — | **47 Pasadas, 0 Fallos, 0 Errores (100% Exitoso)** |

### 4.2. Matriz de Casos de Prueba Funcionales (End-to-End con SQL Server)
Se mantienen plenamente operativos los 20 casos de prueba funcionales ejecutados sobre la base de datos real `DB_BoticaBiofarma`:
- **CP-AUTH-01 a CP-AUTH-05:** Login exitoso y fallido de Administradora y Trabajadora.
- **CP-AUTH-06 a CP-AUTH-10:** Manejo de usuarios inactivos, credenciales malformadas y estructura de token.
- **CP-AUTH-11 a CP-AUTH-15:** Protección RBAC de endpoints de usuarios y bloqueo HTTP 403.
- **CP-AUTH-16 a CP-AUTH-20:** Cierre de sesión, rechazo de tokens alterados y verificación en Frontend React.

### 4.3. Auditoría Técnica de Advertencias y Remediaciones en el Entorno de Ejecución
Tras una inspección rigurosa de los logs de compilación y pruebas de Maven, se resolvieron las siguientes 6 advertencias técnicas:
1. **API obsoleta en `JwtAuthenticationFilter`:** Se eliminó la anotación en desuso `org.springframework.lang.NonNull`, empleando la firma canónica de `OncePerRequestFilter`.
2. **Carga dinámica de agentes Java en Mockito:** Se configuró el plugin `maven-surefire-plugin` con el argumento JVM `-XX:+EnableDynamicAgentLoading -Xshare:off`, cumpliendo con la especificación JEP 451 de Java 21+.
3. **Dialecto redundante de H2:** Se retiró `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect` en el entorno de pruebas, permitiendo que Hibernate 7 detecte automáticamente el dialecto de conexión.
4. **Alerta de `spring.jpa.open-in-view`:** Se configuró explícitamente `spring.jpa.open-in-view=false` tanto en el perfil de producción como en el de pruebas, alineándose con la arquitectura de API REST sin estado.
5. **Generación automática de contraseña de desarrollo en Spring Security:** Se declaró un bean explícito `UserDetailsService` en `SecurityConfig` que formaliza el rechazo de accesos basados en formulario/básica, garantizando que el acceso a la API se gestione **exclusivamente mediante tokens JWT**.
6. **Consulta de tabla `USUARIOS` en H2 cuando no existe (`AdminPasswordInitializer`):** Se configuró `spring.jpa.hibernate.ddl-auto=create-drop` en `src/test/resources/application.properties` para garantizar que la base de datos en memoria H2 genere el esquema relacional aislado antes de la ejecución de los componentes de arranque. Adicionalmente, se implementó `AdminPasswordInitializerTest` con 4 casos unitarios herméticos.

---

## 5. CONCLUSIONES Y RECOMENDACIONES DE ADOPCIÓN DE TDD PARA LOS SIGUIENTES SPRINTS

### 5.1. Conclusiones
1. **Adopción Real de TDD:** Se aplicó estrictamente la técnica Test-First con evidencias comprobables de fallo en compilación y aserción Mockito (Fase RED), implementación mínima satisfactoria (Fase GREEN) y reestructuración de código sin regresión (Fase REFACTOR).
2. **Fortalecimiento de Clean Architecture:** Al mover la lógica de decisión a `Usuario.puedeAutenticarse()`, se recuperó la pureza del Dominio, reduciendo la responsabilidad del Caso de Uso y evitando consultas costosas e innecesarias a SQL Server en credenciales en blanco.
3. **Crecimiento de la Red de Seguridad:** La suite automatizada aumentó de 37 a **43 pruebas** (incremento del 16.2%), cubriendo por primera vez de forma directa la capa de dominio puro sin dependencias externas.

### 5.2. Recomendaciones de Ingeniería para los Siguientes Sprints (HU-03 a HU-12)
- **Priorizar Pruebas de Dominio Primero:** Para la lógica de control de stock, lotes y política FEFO (First Expired, First Out) en ventas (HU-08), escribir primero pruebas unitarias puras sobre las entidades `Lote` y `Venta` antes de implementar servicios o repositorios.
- **Evitar la Complacencia de "Test-Last":** Escribir pruebas después de programar suele sesgar al desarrollador a probar únicamente el "camino feliz". TDD obliga a concebir tempranamente escenarios adversos (stocks negativos, fechas pasadas, nulos).
- **Mantener la Separación Estricta:** Las pruebas de dominio deben ejecutarse en milisegundos sin levantar el contexto de Spring Boot ni conexiones a base de datos. Reservar `@SpringBootTest` y MockMvc únicamente para la capa de presentación y seguridad.

---
*Documento elaborado siguiendo las directrices de Extreme Programming (XP) y Clean Architecture para el proyecto Botica Biofarma.*
