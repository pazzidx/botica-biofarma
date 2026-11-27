USE DB_BoticaBiofarma;
GO

-- HU-04: Agregar precio de venta e indicador de receta médica retenida a Productos
IF COL_LENGTH('Productos', 'precio_venta') IS NULL
BEGIN
    ALTER TABLE Productos
    ADD precio_venta DECIMAL(10,2) NOT NULL
        CONSTRAINT DF_Productos_PrecioVenta DEFAULT (0)
        CONSTRAINT CK_Productos_PrecioVenta CHECK (precio_venta >= 0);
END
GO

IF COL_LENGTH('Productos', 'requiere_receta') IS NULL
BEGIN
    ALTER TABLE Productos
    ADD requiere_receta BIT NOT NULL
        CONSTRAINT DF_Productos_RequiereReceta DEFAULT (0);
END
GO
