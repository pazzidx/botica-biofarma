# Reporte Técnico: Corrección de Integración HU-02 (BIO-6) y HU-01

## 1. Resumen Ejecutivo

| Métrica / Parámetro | Detalle |
| :--- | :--- |
| **Fecha de Ejecución** | 26 de Noviembre de 2026 |
| **Rama de Trabajo** | `fix/correccion-integracion-hu02-hu01` |
| **Commit Base de Integración** | `b233fa5` (Merge pull request #2) |
| **Pruebas Totales** | **63 pruebas automatizadas** |
| **Resultado Previo a la Corrección** | 63 ejecutadas, 5 fallas (*Failures*), 6 errores (*Errors*), BUILD FAILURE |
| **Resultado Posterior a la Corrección** | **63 ejecutadas, 0 fallas, 0 errores, 0 omitidas, BUILD SUCCESS** |
| **Estado Frontend** | `npm run lint`: 0 errores, 0 advertencias; `npm run build`: Exitoso |
| **Archivos de Producción Modificados** | **0** (El código de producción de `src/main` se mantuvo intacto) |

---

## 2. Verificación de Seguridad y Ramas de Respaldo

Conforme al protocolo de aseguramiento, antes de aplicar cambios se verificaron y crearon las siguientes ramas de respaldo independientes en Git:

1. **`respaldo/HU-02-original`**:
   - Apunta al commit `d4de2f6324d8b02ea738b6c9491f3efed28ba7d5`.
   - Preserva la entrega original pura de HU-02 (BIO-29 a BIO-33).
2. **`respaldo/HU-02-integracion`**:
   - Apunta al commit `b233fa59e80f7301190e5a81799e3dddca4c7b5e`.
   - Preserva el estado exacto de la integración inicial con HU-01 antes de las correcciones.
3. **Rama de Corrección**:
   - `fix/correccion-integracion-hu02-hu01` creada a partir de `b233fa5`. La rama `main` quedó completamente protegida sin modificaciones directas.

---

## 3. Diagnóstico de Causa Raíz de los Fallos

Al fusionar la historia HU-01 (gestión ampliada de usuarios: edición rápida, baja inteligente, reactivación) con HU-02 (infraestructura de seguridad JWT y RBAC con roles `ADMINISTRADORA` y `TRABAJADORA`), se produjeron dos desajustes a nivel de pruebas:

### A. Fallas en `UsuarioControllerTest.java` (5 Failures)
- **Síntoma:** Cinco pruebas esperaban códigos `200 OK`, `400 Bad Request` o `403 Forbidden` (por regla de negocio al intentar eliminar la administradora), pero todas recibían `401 Unauthorized`.
- **Pruebas Afectadas:**
  1. `deberiaActualizarNombresYApellidosEnPut` (Esperaba 200, obtuvo 401)
  2. `deberiaRechazarEdicionConNumerosEnPut` (Esperaba 400, obtuvo 401)
  3. `deberiaEliminarFisicamenteSiCeroVentasYDesactivarSiTieneVentas` (Esperaba 200, obtuvo 401)
  4. `deberiaRetornarForbiddenAlIntentarEliminarAdmin` (Esperaba 403, obtuvo 401)
  5. `deberiaReactivarUsuarioEnPatch` (Esperaba 200, obtuvo 401)
- **Causa Raíz:** En HU-02 se blindó toda la ruta `/api/v1/usuarios/**` con `@PreAuthorize("hasRole('ADMINISTRADORA')")` y reglas en `SecurityConfig`. Las 5 pruebas agregadas en HU-01 no tenían la anotación `@WithMockUser(username = "admin", roles = {"ADMINISTRADORA"})`. Spring Security interceptó las solicitudes y retornó `401 Unauthorized` de inmediato, sin permitir que las peticiones llegaran a los métodos del controlador.

### B. Errores en `AutorizacionRolesTest.java` (6 Errors)
- **Síntoma:** Error de arranque del contexto: `java.lang.IllegalStateException: Failed to load ApplicationContext`.
- **Excepción Raíz:** `Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'usuarioController' ... No qualifying bean of type 'com.biofarma.inventario.application.usecase.ActualizarUsuarioUseCase' available`.
- **Causa Raíz:** HU-01 amplió el constructor de `UsuarioController` para inyectar 3 casos de uso adicionales (`ActualizarUsuarioUseCase`, `EliminarUsuarioUseCase`, `ActivarUsuarioUseCase`). La clase de prueba `AutorizacionRolesTest` (que utiliza `@WebMvcTest(controllers = UsuarioController.class)`) solo tenía declarados mocks para `RegistrarUsuarioUseCase` y `ListarUsuariosUseCase`. Al faltar la simulación de las tres nuevas dependencias, el contenedor de inversión de control (IoC) de Spring no pudo instanciar el controlador.

---

## 4. Modificaciones Realizadas

Las correcciones se realizaron de forma quirúrgica exclusivamente en los archivos de prueba, sin alterar código de producción ni deshabilitar ninguna comprobación de seguridad:

### 1. `backend/src/test/java/com/biofarma/inventario/presentation/controller/UsuarioControllerTest.java`
Se incorporó `@WithMockUser(username = "admin", roles = {"ADMINISTRADORA"})` y `@DisplayName` descriptivo en las 5 pruebas administrativas de HU-01:
```java
// Ejemplo de ajuste aplicado:
@Test
@WithMockUser(username = "admin", roles = {"ADMINISTRADORA"})
@DisplayName("Debería actualizar nombres y apellidos mediante PUT cuando lo solicita ADMINISTRADORA")
void deberiaActualizarNombresYApellidosEnPut() throws Exception { ... }
```
Se preservaron sin modificaciones las pruebas que intencionalmente validan el rechazo sin token (`401 Unauthorized`) o con rol no autorizado `TRABAJADORA` (`403 Forbidden`).

### 2. `backend/src/test/java/com/biofarma/inventario/infrastructure/security/AutorizacionRolesTest.java`
Se importaron e inyectaron las tres dependencias requeridas por `UsuarioController`:
```java
@MockitoBean
private ActualizarUsuarioUseCase actualizarUsuarioUseCase;

@MockitoBean
private EliminarUsuarioUseCase eliminarUsuarioUseCase;

@MockitoBean
private ActivarUsuarioUseCase activarUsuarioUseCase;
```
Se conservaron intactas las pruebas de validación de firma JWT, expiración de tokens, bloqueo por usuario inactivo y actualización dinámica de roles en base de datos.

---

## 5. Resultados de las Pruebas

### A. Pruebas Unitarias e Integración Afectadas
```text
[INFO] Running com.biofarma.inventario.infrastructure.security.AutorizacionRolesTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.647 s
[INFO] Running com.biofarma.inventario.presentation.controller.UsuarioControllerTest
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.221 s
[INFO] Tests run: 19, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### B. Suite Completa Maven (`mvn clean test`)
```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.biofarma.inventario.application.usecase.ActivarUsuarioUseCaseTest (1 test) -> PASSED
[INFO] Running com.biofarma.inventario.application.usecase.ActualizarUsuarioUseCaseTest (4 tests) -> PASSED
[INFO] Running com.biofarma.inventario.application.usecase.CerrarSesionUseCaseTest (1 test) -> PASSED
[INFO] Running com.biofarma.inventario.application.usecase.EliminarUsuarioUseCaseTest (4 tests) -> PASSED
[INFO] Running com.biofarma.inventario.application.usecase.IniciarSesionUseCaseTest (7 tests) -> PASSED
[INFO] Running com.biofarma.inventario.application.usecase.RegistrarUsuarioUseCaseTest (6 tests) -> PASSED
[INFO] Running com.biofarma.inventario.BackendApplicationTests (1 test) -> PASSED
[INFO] Running com.biofarma.inventario.domain.model.UsuarioTest (3 tests) -> PASSED
[INFO] Running com.biofarma.inventario.infrastructure.initialization.AdminPasswordInitializerTest (4 tests) -> PASSED
[INFO] Running com.biofarma.inventario.infrastructure.security.AutorizacionRolesTest (6 tests) -> PASSED
[INFO] Running com.biofarma.inventario.infrastructure.security.JwtServiceTest (6 tests) -> PASSED
[INFO] Running com.biofarma.inventario.presentation.controller.AuthControllerTest (7 tests) -> PASSED
[INFO] Running com.biofarma.inventario.presentation.controller.UsuarioControllerTest (13 tests) -> PASSED
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 63, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 22.100 s
```

### C. Verificación de Frontend y Calidad de Código
1. **Linter Frontend (`npm run lint`):**
   - Ejecutado con `oxlint` sobre 14 archivos.
   - **Resultado:** `Found 0 warnings and 0 errors.`
2. **Compilación Frontend (`npm run build`):**
   - Ejecutado con Vite 8.3.8.
   - **Resultado:** `built in 1.12s` sin advertencias.
3. **Formato y Espacios (`git diff --check`):**
   - **Resultado:** 0 errores de formato, fin de línea o espacios en blanco.

---

## 6. Verificación de Protección de Endpoints

Se confirmó la vigencia del modelo de seguridad multicapa (Defensa en Profundidad):

| Endpoint | Método HTTP | Restricción de Acceso | Validación de Seguridad |
| :--- | :---: | :---: | :---: |
| `/api/v1/auth/login` | `POST` | Público (`permitAll()`) | Verificación de credenciales BCrypt y estado activo. |
| `/api/v1/auth/logout` | `POST` | Autenticado (`authenticated()`) | Token JWT válido y contexto de seguridad autenticado. |
| `/api/v1/usuarios` | `POST` | Exclusivo `ADMINISTRADORA` | `@PreAuthorize` + `hasRole('ADMINISTRADORA')` en filtro. |
| `/api/v1/usuarios` | `GET` | Exclusivo `ADMINISTRADORA` | `@PreAuthorize` + `hasRole('ADMINISTRADORA')` en filtro. |
| `/api/v1/usuarios/{id}` | `PUT` | Exclusivo `ADMINISTRADORA` | `@PreAuthorize` + `hasRole('ADMINISTRADORA')` en filtro. |
| `/api/v1/usuarios/{id}` | `DELETE` | Exclusivo `ADMINISTRADORA` | `@PreAuthorize` + `hasRole('ADMINISTRADORA')` en filtro. |
| `/api/v1/usuarios/{id}/activar` | `PATCH` | Exclusivo `ADMINISTRADORA` | `@PreAuthorize` + `hasRole('ADMINISTRADORA')` en filtro. |

---

## 7. Estado Final de Git

- **Rama actual:** `fix/correccion-integracion-hu02-hu01`.
- **Ramas de respaldo existentes:**
  - `respaldo/HU-02-original` (`d4de2f6`)
  - `respaldo/HU-02-integracion` (`b233fa5`)
- **Archivos modificados (pendientes de commit por el usuario):**
  - `backend/src/test/java/com/biofarma/inventario/infrastructure/security/AutorizacionRolesTest.java`
  - `backend/src/test/java/com/biofarma/inventario/presentation/controller/UsuarioControllerTest.java`
  - `HU-02_CORRECCION_INTEGRACION.md`
- **Restricciones respetadas:** No se ejecutó `git add`, `git commit`, `git push`, `git pull`, `git merge` ni `git reset --hard`.
