USE DB_BoticaBiofarma;
GO

-- =============================================================================
-- Tablas de Ventas y DetalleVentas (Soporte para HU-03 / HU-08)
-- =============================================================================

IF OBJECT_ID('dbo.Ventas', 'U') IS NULL
BEGIN
    CREATE TABLE Ventas (
        id_venta INT IDENTITY(1,1) PRIMARY KEY,
        numero_ticket VARCHAR(30) NOT NULL UNIQUE,
        id_usuario INT NOT NULL,
        fecha_venta DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        total DECIMAL(10,2) NOT NULL CHECK (total >= 0),
        estado VARCHAR(20) NOT NULL DEFAULT 'COMPLETADA',
        CONSTRAINT FK_Ventas_Usuarios FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario)
    );

    CREATE INDEX IX_Ventas_Usuario_Fecha ON Ventas(id_usuario, fecha_venta DESC);
END
GO

IF OBJECT_ID('dbo.DetalleVentas', 'U') IS NULL
BEGIN
    CREATE TABLE DetalleVentas (
        id_detalle INT IDENTITY(1,1) PRIMARY KEY,
        id_venta INT NOT NULL,
        id_lote INT NOT NULL,
        nombre_producto VARCHAR(150) NOT NULL,
        numero_lote VARCHAR(50) NOT NULL,
        cantidad INT NOT NULL CHECK (cantidad > 0),
        precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
        subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
        CONSTRAINT FK_DetalleVentas_Ventas FOREIGN KEY (id_venta) REFERENCES Ventas(id_venta),
        CONSTRAINT FK_DetalleVentas_Lotes FOREIGN KEY (id_lote) REFERENCES Lotes(id_lote)
    );

    CREATE INDEX IX_DetalleVentas_Venta ON DetalleVentas(id_venta);
END
GO
