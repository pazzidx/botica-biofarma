-- ============================================================================
-- BASE DE DATOS COMPLETA: DB_BoticaBiofarma (SQL Server)
-- Cubre HU-01 hasta HU-12 | Listo para ejecución idempotente (IF NOT EXISTS)
-- ============================================================================

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'DB_BoticaBiofarma')
BEGIN
    CREATE DATABASE DB_BoticaBiofarma;
END
GO

USE DB_BoticaBiofarma;
GO

-- 1. ROLES (HU-01, HU-02)
IF OBJECT_ID(N'dbo.roles', N'U') IS NULL
BEGIN
CREATE TABLE dbo.roles (
                           id_rol INT IDENTITY(1,1) PRIMARY KEY,
                           nombre VARCHAR(30) NOT NULL UNIQUE -- 'ADMINISTRADORA', 'TRABAJADORA'
);
END
GO

-- 2. USUARIOS (HU-01, HU-02, HU-03, HU-11)
IF OBJECT_ID(N'dbo.usuarios', N'U') IS NULL
BEGIN
CREATE TABLE dbo.usuarios (
                              id_usuario INT IDENTITY(1,1) PRIMARY KEY,
                              codigo_usuario VARCHAR(30) NOT NULL UNIQUE,
                              nombres VARCHAR(100) NOT NULL,
                              apellidos VARCHAR(100) NOT NULL,
                              password_hash VARCHAR(255) NOT NULL,
                              id_rol INT NOT NULL,
                              activo BIT NOT NULL DEFAULT 1,
                              fecha_registro DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                              CONSTRAINT fk_usuarios_rol FOREIGN KEY (id_rol) REFERENCES dbo.roles(id_rol)
);
END
GO

-- 3. MEDICAMENTOS (HU-04, HU-05, HU-07, HU-10)
IF OBJECT_ID(N'dbo.medicamentos', N'U') IS NULL
BEGIN
CREATE TABLE dbo.medicamentos (
                                  id_medicamento INT IDENTITY(1,1) PRIMARY KEY,
                                  codigo VARCHAR(30) NOT NULL UNIQUE,
                                  nombre VARCHAR(150) NOT NULL,
                                  presentacion VARCHAR(100) NOT NULL,
                                  precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
                                  stock_actual INT NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
                                  stock_minimo INT NOT NULL DEFAULT 5 CHECK (stock_minimo >= 0),
                                  requiere_receta BIT NOT NULL DEFAULT 0,
                                  activo BIT NOT NULL DEFAULT 1,
                                  fecha_creacion DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
CREATE INDEX idx_medicamentos_nombre ON dbo.medicamentos(nombre);
END
GO

-- 4. LOTES (HU-06, HU-07)
IF OBJECT_ID(N'dbo.lotes', N'U') IS NULL
BEGIN
CREATE TABLE dbo.lotes (
                           id_lote INT IDENTITY(1,1) PRIMARY KEY,
                           id_medicamento INT NOT NULL,
                           numero_lote VARCHAR(50) NOT NULL,
                           cantidad_disponible INT NOT NULL CHECK (cantidad_disponible >= 0),
                           fecha_vencimiento DATE NOT NULL,
                           fecha_registro DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                           CONSTRAINT fk_lotes_medicamento FOREIGN KEY (id_medicamento) REFERENCES dbo.medicamentos(id_medicamento),
                           CONSTRAINT uq_lote_medicamento UNIQUE (id_medicamento, numero_lote)
);
CREATE INDEX idx_lotes_vencimiento ON dbo.lotes(fecha_vencimiento);
END
GO

-- 5. INGRESOS DE INVENTARIO (HU-06)
IF OBJECT_ID(N'dbo.ingresos_inventario', N'U') IS NULL
BEGIN
CREATE TABLE dbo.ingresos_inventario (
                                         id_ingreso INT IDENTITY(1,1) PRIMARY KEY,
                                         id_medicamento INT NOT NULL,
                                         id_lote INT NOT NULL,
                                         id_usuario INT NOT NULL,
                                         cantidad_recibida INT NOT NULL CHECK (cantidad_recibida > 0),
                                         observacion_faltantes VARCHAR(255) NULL,
                                         fecha_ingreso DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                                         CONSTRAINT fk_ingresos_medicamento FOREIGN KEY (id_medicamento) REFERENCES dbo.medicamentos(id_medicamento),
                                         CONSTRAINT fk_ingresos_lote FOREIGN KEY (id_lote) REFERENCES dbo.lotes(id_lote),
                                         CONSTRAINT fk_ingresos_usuario FOREIGN KEY (id_usuario) REFERENCES dbo.usuarios(id_usuario)
);
END
GO

-- 6. VENTAS (HU-01 baja lógica, HU-03, HU-08, HU-09, HU-10, HU-11, HU-12)
IF OBJECT_ID(N'dbo.ventas', N'U') IS NULL
BEGIN
CREATE TABLE dbo.ventas (
                            id_venta INT IDENTITY(1,1) PRIMARY KEY,
                            numero_ticket VARCHAR(20) NOT NULL UNIQUE,
                            id_usuario INT NOT NULL, -- Trabajadora responsable (JWT)
                            total DECIMAL(10,2) NOT NULL CHECK (total >= 0),
                            codigo_receta VARCHAR(50) NULL,       -- HU-10: Constancia receta retenida
                            observacion_receta VARCHAR(255) NULL, -- HU-10: Observación receta física
                            fecha_venta DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
                            CONSTRAINT fk_ventas_usuario FOREIGN KEY (id_usuario) REFERENCES dbo.usuarios(id_usuario)
);
CREATE INDEX idx_ventas_fecha ON dbo.ventas(fecha_venta);
CREATE INDEX idx_ventas_usuario ON dbo.ventas(id_usuario);
END
GO

-- 7. DETALLE DE VENTAS (HU-08, HU-09, HU-12)
IF OBJECT_ID(N'dbo.detalle_ventas', N'U') IS NULL
BEGIN
CREATE TABLE dbo.detalle_ventas (
                                    id_detalle INT IDENTITY(1,1) PRIMARY KEY,
                                    id_venta INT NOT NULL,
                                    id_medicamento INT NOT NULL,
                                    cantidad INT NOT NULL CHECK (cantidad > 0),
                                    precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
                                    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
                                    CONSTRAINT fk_detalle_venta FOREIGN KEY (id_venta) REFERENCES dbo.ventas(id_venta),
                                    CONSTRAINT fk_detalle_medicamento FOREIGN KEY (id_medicamento) REFERENCES dbo.medicamentos(id_medicamento)
);
END
GO

-- ============================================================================
-- DATOS SEMILLA INICIALES (Roles + Única Administradora)
-- ============================================================================
IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE nombre = 'ADMINISTRADORA')
    INSERT INTO dbo.roles (nombre) VALUES ('ADMINISTRADORA');

IF NOT EXISTS (SELECT 1 FROM dbo.roles WHERE nombre = 'TRABAJADORA')
    INSERT INTO dbo.roles (nombre) VALUES ('TRABAJADORA');
GO

-- Cuenta única ADMINISTRADORA (clave: admin123 cifrada con BCrypt)
IF NOT EXISTS (SELECT 1 FROM dbo.usuarios WHERE codigo_usuario = 'admin')
BEGIN
    DECLARE @idRolAdmin INT = (SELECT id_rol FROM dbo.roles WHERE nombre = 'ADMINISTRADORA');
INSERT INTO dbo.usuarios (codigo_usuario, nombres, apellidos, password_hash, id_rol, activo)
VALUES (
           'admin',
           'Propietaria',
           'Biofarma',
           '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
           @idRolAdmin,
           1
       );
END
GO