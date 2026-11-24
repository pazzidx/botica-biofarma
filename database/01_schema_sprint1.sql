CREATE DATABASE DB_BoticaBiofarma;
GO

USE DB_BoticaBiofarma;
GO

-- 1. Roles del sistema (HU-01)
CREATE TABLE Roles (
    id_rol INT IDENTITY(1,1) PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO Roles (nombre) VALUES ('ADMINISTRADORA'), ('TRABAJADORA');
GO

-- 2. Usuarios (HU-01 y HU-02)
CREATE TABLE Usuarios (
    id_usuario INT IDENTITY(1,1) PRIMARY KEY,
    codigo_usuario VARCHAR(20) NOT NULL UNIQUE,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    activo BIT NOT NULL DEFAULT 1,
    fecha_creacion DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Usuarios_Roles FOREIGN KEY (id_rol) REFERENCES Roles(id_rol)
);

-- Usuario inicial -> Usuario: admin | Clave: Admin123* (cifrada con BCrypt)
INSERT INTO Usuarios (codigo_usuario, nombres, apellidos, password_hash, id_rol)
VALUES (
    'admin',
    'Propietaria',
    'Biofarma',
    '',
    1
);
GO

-- 3. Productos Farmaceuticos (HU-03)
CREATE TABLE Productos (
    id_producto INT IDENTITY(1,1) PRIMARY KEY,
    codigo_producto VARCHAR(30) NOT NULL UNIQUE,
    nombre_comercial VARCHAR(150) NOT NULL,
    principio_activo VARCHAR(150) NOT NULL,
    presentacion VARCHAR(80) NOT NULL,
    laboratorio VARCHAR(100) NULL,
    stock_minimo INT NOT NULL CHECK (stock_minimo >= 0),
    activo BIT NOT NULL DEFAULT 1,
    fecha_registro DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);
GO

-- 4. Lotes por Producto (HU-04)
CREATE TABLE Lotes (
    id_lote INT IDENTITY(1,1) PRIMARY KEY,
    id_producto INT NOT NULL,
    numero_lote VARCHAR(50) NOT NULL UNIQUE,
    registro_sanitario VARCHAR(50) NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    cantidad_inicial INT NOT NULL CHECK (cantidad_inicial > 0),
    cantidad_actual INT NOT NULL CHECK (cantidad_actual >= 0),
    precio_compra DECIMAL(10,2) NOT NULL CHECK (precio_compra >= 0),
    precio_venta DECIMAL(10,2) NOT NULL CHECK (precio_venta >= 0),
    fecha_ingreso DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    CONSTRAINT FK_Lotes_Productos FOREIGN KEY (id_producto) REFERENCES Productos(id_producto)
);

CREATE INDEX IX_Lotes_FechaVencimiento ON Lotes(id_producto, fecha_vencimiento ASC);
GO
