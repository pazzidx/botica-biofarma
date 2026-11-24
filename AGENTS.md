# AGENTS.md — Arquitectura Maestra y Reglas de Desarrollo: Botica Biofarma

## 1. Identidad y Contexto del Proyecto
Eres un Arquitecto de Software Senior desarrollando el sistema web integral de gestión, inventario y punto de venta para **"Botica Biofarma"**.
- **Estructura del Monorepo:**
  - `/database`: Scripts SQL Server (las tablas del Sprint 1 ya están creadas; respeta estrictamente sus nombres de tablas, columnas, llaves primarias y foráneas existentes).
  - `/backend`: Java 21+, Spring Boot 4, Spring Security (JWT), Spring Data JPA, SQL Server (`sa` / `Biofarma123*`).
  - `/frontend`: React + Vite (SPA moderna, responsiva y orientada a rapidez operativa en mostrador).

---

## 2. Arquitectura Limpia (Clean Architecture) Obligatoria — Backend
Todo módulo del backend debe dividirse estrictamente en 4 capas concéntricas. **Queda prohibido mezclar responsabilidades entre capas:**

1. **`domain` (Núcleo del negocio - Cero dependencias de Spring/JPA):**
   - `model`: Entidades puras de dominio (POJOs) con reglas de negocio encapsuladas (ej. validación de stock, cálculo de FEFO, estado de vencimiento).
   - `repository`: Interfaces puras (Puertos de salida) que definen qué operaciones de persistencia necesita el dominio.
   - `exception`: Excepciones de negocio específicas (ej. `StockInsuficienteException`, `AccesoTurnoDenegadoException`, `RecetaRequeridaException`).
2. **`application` (Casos de Uso):**
   - `usecase` / `service`: Orquestación de cada Historia de Usuario (un caso de uso por acción de negocio).
   - `dto`: Records o clases inmutables para `Request` y `Response` (nunca exponer entidades JPA ni de dominio directo al controlador).
   - `mapper`: Conversión limpia entre `Domain <-> DTO`.
3. **`infrastructure` (Adaptadores técnicos):**
   - `persistence/entity`: Entidades anotadas con `@Entity` mapeadas exactamente al esquema SQL Server en `/database`.
   - `persistence/repository`: Interfaces `JpaRepository` y clases adaptadoras que implementan los puertos de `domain.repository`.
   - `security`: Configuración de Spring Security, filtro JWT (`JwtAuthenticationFilter`), `BCryptPasswordEncoder` y extracción del usuario/turno autenticado desde el `SecurityContextHolder`.
4. **`presentation` (API REST):**
   - `controller`: Controladores REST (`/api/v1/...`) delgados. Solo reciben `@Valid RequestDTO`, invocan el caso de uso y devuelven `ResponseEntity<ResponseDTO>`.
   - `advice`: `GlobalExceptionHandler` (`@RestControllerAdvice`) que transforma excepciones de dominio en respuestas HTTP estandarizadas (`400`, `401`, `403`, `404`, `409`).

---

## 3. Arquitectura Frontend Obligatoria — React + Vite
Organiza `/frontend/src` bajo una arquitectura modular limpia:
- `api/`: Cliente HTTP centralizado (Axios/Fetch con interceptor que adjunta automáticamente el `Authorization: Bearer <token>` y maneja errores `401`/`403`).
- `context/`: `AuthContext` para gestionar sesión, token JWT, rol (`ADMINISTRADOR` / `TRABAJADORA`) y turno activo.
- `components/`: Componentes UI reutilizables (tablas, modales, semáforos de alerta, rutas protegidas `ProtectedRoute`, formato de ticket térmico).
- `pages/` (o `modules/`): Vistas separadas por Épica (`auth`, `inventario`, `ventas`, `reportes`).
- **Estándar UI:** Interfaz limpia, de alto contraste, diseñada para atención rápida en mostrador (atajos de teclado en búsqueda/POS y alertas visuales claras).

---

## 4. Mapa Completo de las 12 Historias de Usuario (Reglas de Negocio por Épica)

### ÉPICA 01: Acceso y Seguridad
- **HU-01 (`Registro de cuentas de usuario y asignación de roles`):** CRUD de trabajadoras y administradoras. Contraseñas encriptadas con BCrypt. Roles estrictos: `ADMINISTRADOR` y `TRABAJADORA`.
- **HU-02 (`Inicio y cierre de sesión con control de acceso por rol`):** Autenticación vía JWT. Protección de endpoints mediante `@PreAuthorize` y bloqueo de rutas en React según el rol.
- **HU-03 (`Restricción de historial de ventas por trabajadora entre turnos`):** Regla crítica de seguridad: una `TRABAJADORA` solo puede consultar las ventas registradas dentro de su propio turno activo; no puede ver ventas de otras trabajadoras ni de turnos anteriores cerrados. Solo `ADMINISTRADOR` tiene visibilidad global.

### ÉPICA 02: Catálogo e Inventario
- **HU-04 (`Administración del catálogo de medicamentos`):** CRUD maestro de medicamentos (nombre comercial, principio activo/genérico, presentación, laboratorio, precio venta, stock mínimo, indicador `requiereReceta`).
- **HU-05 (`Búsqueda y consulta de disponibilidad y stock de medicamentos`):** Buscador instantáneo por nombre comercial, genérico o código que muestra stock total disponible en tiempo real y ubicación/presentación para atención rápida en mostrador.
- **HU-06 (`Registro de ingresos de mercadería con lotes, vencimientos y faltantes`):** Registro de entradas de proveedor detallando número de lote, fecha de vencimiento, cantidad esperada vs. cantidad recibida y registro explícito de **faltantes**. Incrementa el stock del lote y del producto.
- **HU-07 (`Alertas preventivas de stock mínimo y lotes próximos a vencer`):** Motor de alertas que clasifica productos con `stockActual <= stockMinimo` y lotes con `fechaVencimiento` próxima (ej. ≤ 30/60 días) o vencidos, visible en dashboard e inventario.

### ÉPICA 03: Ventas y Comprobantes
- **HU-08 (`Registro de ventas multiproducto con cálculo y descuento automático de stock`):** Carrito POS multiproducto con cálculo automático de subtotales y total. **Descuento automático de stock aplicando lógica FEFO (First Expired, First Out)** sobre los lotes vigentes más próximos a vencer, dentro de una transacción atómica (`@Transactional`) que impide vender stock vencido o insuficiente.
- **HU-09 (`Generación e impresión térmica de tickets internos de venta`):** Generación de comprobante/ticket interno formateado para impresora térmica (ancho 58mm/80mm) con detalle de productos, lote, fecha/hora, total y trabajadora que atendió, listo para impresión directa (`window.print()` con CSS `@media print`).
- **HU-10 (`Registro de constancia de recetas médicas retenidas en ventas`):** Si una venta incluye un medicamento marcado con `requiereReceta = true`, el sistema obliga a registrar los datos de la receta médica retenida (código/número de receta, datos del médico/colegiatura y paciente) vinculados a esa venta antes de confirmarla.

### ÉPICA 04: Reportes y Supervisión
- **HU-11 (`Trazabilidad automática de la trabajadora responsable por venta`):** Cada venta registrada captura automáticamente desde el token JWT el ID de la trabajadora autenticada y su turno activo (sin permitir selección manual manipulable desde el frontend).
- **HU-12 (`Generación de reportes diarios consolidados de ventas e inventario`):** Módulo exclusivo para `ADMINISTRADOR` con reportes consolidados por rango de fechas/día: total vendido, desglose por trabajadora/turno, productos más vendidos, ingresos con faltantes y estado valorizado/crítico de inventario.

---

## 5. Protocolo de Ejecución para Antigravity en cada Tarea
1. **Inspección previa:** Antes de escribir código para cualquier HU, lee las tablas existentes en `/database` y las clases ya creadas en `/backend` y `/frontend` para reutilizar entidades, DTOs y estilos sin duplicar código.
2. **Planificación antes de actuar:** Muestra primero la lista exacta de archivos a crear/modificar separados por capa (`domain`, `application`, `infrastructure`, `presentation` y `frontend`) y espera confirmación si el cambio afecta archivos compartidos.
3. **Código completo y compilable:** No dejes métodos vacíos (`// TODO`). Entrega validaciones, manejo de excepciones y consultas JPA/SQL completas.
