# Guía de Desarrollo — Sprint 1: Botica Biofarma

## 1. Contexto Base Obligatorio (Para darle a Antigravity antes de pedir código)
Antes de generar código en Google Antigravity, cada integrante debe asegurarse de tener el archivo `.cursorrules` / reglas de contexto o pasarle este bloque inicial al agente para que no rompa la arquitectura del monorepo:

### Prompt de Contextualización para Antigravity:
> *"Estamos desarrollando el sistema de gestión e inventario 'Botica Biofarma' en un monorepo con 3 carpetas: `/database` (SQL Server), `/backend` (Java 21+, Spring Boot 4, Clean Architecture dividida en capas domain, application, infrastructure, presentation) y `/frontend` (React + Vite). La base de datos corre en SQL Server con usuario `sa` y contraseña `Biofarma123*`. Todo el código nuevo debe respetar estrictamente la Clean Architecture en backend (sin lógica de negocio en los controllers, usando DTOs y mappers) y componentes funcionales limpios en frontend."*

---

## 2. Desglose Técnico por Historia de Usuario (Sprint 1)

### 🔵 GRUPO A: Jover, Angelo y Miler
**Ramas Git:** `feature/BIO-5-[nombre]` y `feature/BIO-6-[nombre]`

#### 📌 HU-01 (BIO-5): Inicio de sesión y control de roles
- **Objetivo funcional:** Permitir que los usuarios registrados ingresen al sistema mediante credenciales válidas, generando un token de sesión seguro e identificando su rol (`ADMINISTRADOR` o `VENDEDOR`).
- **Qué implementar en Backend (`/backend`):**
  - **Domain:** Entidad `Usuario` y `Rol`, interfaces de repositorio (`UsuarioRepository`).
  - **Application:** Caso de uso `IniciarSesionUseCase` + DTOs (`LoginRequest`, `LoginResponse` con token JWT y datos básicos del usuario).
  - **Infrastructure:** Implementación JPA/SQL Server, configuración de Spring Security, encriptación de contraseñas con `BCryptPasswordEncoder` y utilitario `JwtService` para firmar y validar tokens.
  - **Presentation:** `AuthController` con endpoint `POST /api/v1/auth/login` (retorna `200 OK` con token o `401 Unauthorized`).
- **Qué implementar en Frontend (`/frontend`):**
  - Vista `LoginPage` con validación de campos vacíos y manejo de errores visuales (credenciales incorrectas).
  - Servicio `authService.js` (Axios/Fetch) que guarda el JWT y el rol en `localStorage` / Context API (`AuthContext`).
  - Componente `ProtectedRoute` para bloquear el acceso al dashboard si no hay token válido.

#### 📌 HU-02 (BIO-6): Inicio y cierre de sesión con control de acceso por rol
- **Objetivo funcional:** Restringir las vistas y endpoints según el rol del usuario autenticado, además de permitir cerrar sesión de forma segura y gestionar los usuarios del sistema.
- **Qué implementar en Backend (`/backend`):**
  - **Infrastructure:** Filtro `JwtAuthenticationFilter` que intercepta cada petición HTTP, valida el token y carga los permisos (`ROLE_ADMINISTRADOR`, `ROLE_VENDEDOR`) en el `SecurityContextHolder`.
  - **Application / Presentation:** Endpoints protegidos para gestión de usuarios (`GET /api/v1/usuarios`, `POST /api/v1/usuarios`, `PATCH /api/v1/usuarios/{id}/estado`) restringidos solo a `ADMINISTRADOR` (`@PreAuthorize("hasRole('ADMINISTRADOR')")`), y endpoint de cierre de sesión / invalidación o limpieza de sesión.
- **Qué implementar en Frontend (`/frontend`):**
  - Renderizado condicional en el `Sidebar` / `Navbar` (el Vendedor no debe ver módulos exclusivos de Administración).
  - Botón **Cerrar Sesión** que limpia el `localStorage`, resetea el `AuthContext` y redirige a `/login`.
  - Pantalla de gestión de usuarios (crear usuario, asignar rol y activar/desactivar cuenta).

---

### 🟢 GRUPO B: Rony, Micha y Edson
**Ramas Git:** `feature/BIO-7-[nombre]` y `feature/BIO-8-[nombre]`

#### 📌 HU-03 (BIO-7): Gestión de Catálogo de Medicamentos y Categorías
- **Objetivo funcional:** Administrar el maestro de productos farmacéuticos (nombre comercial, genérico, concentración, presentación, laboratorio, categoría, precio y stock mínimo) para tener el catálogo base de la botica.
- **Qué implementar en Backend (`/backend`):**
  - **Domain:** Entidades `Categoria` y `Producto` (con reglas de validación: precio mayor a 0, código único) e interfaces `CategoriaRepository` y `ProductoRepository`.
  - **Application:** Casos de uso CRUD (`CrearProductoUseCase`, `ActualizarProductoUseCase`, `ListarProductosUseCase`, `GestionarCategoriaUseCase`) + DTOs (`ProductoRequest`, `ProductoResponse`).
  - **Infrastructure:** Entidades JPA mapeadas a las tablas de SQL Server en `/database` y repositorios Spring Data JPA.
  - **Presentation:** `ProductoController` (`/api/v1/productos`) y `CategoriaController` (`/api/v1/categorias`) con soporte para búsqueda por nombre o principio activo.
- **Qué implementar en Frontend (`/frontend`):**
  - Vista `CatalogoProductosPage` con tabla paginada/filtrable y barra de búsqueda en tiempo real.
  - Modal o formulario para registrar y editar productos farmacéuticos seleccionando su categoría.
  - Vista o modal rápido para crear/editar categorías.

#### 📌 HU-04 (BIO-8): Gestión de Lotes, Stock y Alertas de Vencimiento
- **Objetivo funcional:** Controlar el ingreso de mercadería por número de lote y fecha de vencimiento (clave en farmacias para aplicar FEFO: *First Expired, First Out*), actualizar el stock total y alertar productos próximos a vencer o con stock bajo.
- **Qué implementar en Backend (`/backend`):**
  - **Domain:** Entidad `Lote` (vinculada a `Producto`, con `numeroLote`, `fechaVencimiento`, `cantidadInicial`, `cantidadActual`, `precioCompra`) y lógica de dominio para detectar estado (`VIGENTE`, `POR_VENCER`, `VENCIDO`).
  - **Application:** Casos de uso `RegistrarLoteUseCase` (que suma automáticamente al stock disponible del producto), `ListarLotesPorProductoUseCase` y `ObtenerAlertasInventarioUseCase` (lotes que vencen en ≤ 30 días o productos debajo del stock mínimo).
  - **Presentation:** `LoteController` (`/api/v1/lotes`) y endpoint de alertas (`/api/v1/inventario/alertas`).
- **Qué implementar en Frontend (`/frontend`):**
  - Formulario de ingreso de lotes vinculado al catálogo de productos.
  - Tabla de lotes con semáforo visual (Verde: vigente, Amarillo/Ámbar: próximo a vencer en 30 días, Rojo: vencido o stock crítico).
  - Panel o widget de notificaciones de alertas en el dashboard principal.

---

## 3. Flujo Exacto para Trabajar con Antigravity y GitHub (Para los 6)

1. **Actualizar rama base:**
   ```bash
   git checkout main
   git pull origin main
   ```

2. **Crear rama de trabajo según tu subtarea:**
   ```bash
   git checkout -b feature/BIO-X-tu-nombre
   ```

3. **Abrir Google Antigravity:**
   - Abre la carpeta del proyecto y pega primero el **Prompt de Contextualización (Sección 1)** junto con la descripción de tu Historia de Usuario (Sección 2).
   - Pídele a Antigravity que primero te muestre el plan de archivos a crear/modificar antes de aplicar cambios masivos, para verificar que respete las carpetas de Clean Architecture (`domain`, `application`, `infrastructure`, `presentation`).

4. **Probar en local y subir Pull Request:**
   - Verifica que Spring Boot compile sin errores y React levante limpio.
   - Registra tus cambios y súbelos:
     ```bash
     git add .
     git commit -m "feat(BIO-X): descripcion clara"
     git push origin feature/BIO-X-tu-nombre
     ```
   - Abre el **Pull Request** hacia `main` en GitHub para revisión del equipo.
