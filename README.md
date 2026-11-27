# Botica Biofarma — Sistema Web de Control de Inventario y Ventas

Sistema web integral de gestión, inventario y punto de venta para la botica farmacéutica **"Botica Biofarma"**.

---

## 1. Arquitectura y Tecnologías
- **Backend:** Java 21+, Spring Boot 4, Spring Security, JWT (HMAC-SHA256), Spring Data JPA, Hibernate.
  - Arquitectura Limpia (Clean Architecture) dividida en capas: `domain`, `application`, `infrastructure` y `presentation`.
- **Frontend:** React 19, Vite, React Router, Axios, Lucide React.
  - Diseño modular con protección de rutas por roles (`ADMINISTRADORA` y `TRABAJADORA`).
- **Base de Datos:** Microsoft SQL Server 2022/2025.
  - Base de datos: `DB_BoticaBiofarma` (esquema en `/database`).

---

## 2. Requisitos Previos
- **Java Development Kit (JDK):** Versión 21 o superior.
- **Node.js:** Versión 18 o superior con `npm`.
- **Microsoft SQL Server:** Instancia local en ejecución con autenticación mixta y protocolo TCP/IP habilitado en el puerto estándar `1433`.

---

## 3. Configuración de Base de Datos
1. Abrir SQL Server Management Studio (SSMS) o ejecutar el script vía consola.
2. Ejecutar el script oficial de esquema:
   ```sql
   database/01_schema_sprint1.sql
   ```
   Esto creará la base de datos `DB_BoticaBiofarma` y las tablas `Roles`, `Usuarios`, `Productos` y `Lotes`.

---

## 4. Ejecución del Backend (Spring Boot)

### Variables de Entorno Requeridas
Configure las siguientes variables en su terminal antes de iniciar el servidor:

```powershell
# Credenciales de conexión a SQL Server
$env:SPRING_DATASOURCE_URL = "jdbc:sqlserver://localhost:1433;databaseName=DB_BoticaBiofarma;encrypt=true;trustServerCertificate=true"
$env:SPRING_DATASOURCE_USERNAME = "<su_usuario_sql>"
$env:SPRING_DATASOURCE_PASSWORD = "<su_password_sql>"

# Clave secreta JWT (cadena segura de al menos 32 caracteres / 256 bits)
$env:JWT_SECRET = "<su_clave_secreta_jwt_minimo_32_caracteres>"

# Vigencia del token en milisegundos (opcional, por defecto 8 horas: 28800000)
$env:JWT_EXPIRATION_MS = "28800000"

# Contraseña inicial para el usuario 'admin' (si el hash inicial está vacío)
$env:INITIAL_ADMIN_PASSWORD = "<su_contraseña_inicial_admin>"
```

### Iniciar el Backend
```powershell
cd backend
mvn spring-boot:run
```
El servidor backend estará disponible en `http://localhost:8080`.

---

## 5. Ejecución del Frontend (React + Vite)

### Iniciar el Frontend
En una nueva ventana de terminal:

```powershell
cd frontend

# Instalar dependencias si es la primera vez
npm install

# Iniciar servidor de desarrollo
npm run dev
```
La aplicación web estará disponible en `http://localhost:5173`.

---

## 6. Endpoints Principales de la API
- **Autenticación:**
  - `POST /api/v1/auth/login`: Inicio de sesión (público).
  - `POST /api/v1/auth/logout`: Cierre de sesión (requiere token JWT).
- **Gestión de Personal (Exclusivo ADMINISTRADORA):**
  - `POST /api/v1/usuarios`: Registro de nuevo usuario.
  - `GET /api/v1/usuarios`: Listado de usuarios registrados.
