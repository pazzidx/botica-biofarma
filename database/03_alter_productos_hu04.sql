-- ============================================================================
-- SCRIPT: 03_alter_productos_hu04.sql
-- PROYECTO: Botica Biofarma
-- HISTORIA DE USUARIO: HU-04 (BIO-8) - Administracion del catalogo de medicamentos
-- OBJETIVO: Agregar de forma idempotente las columnas requeridas en la tabla
--           Productos del Sprint 1 sin perder datos existentes.
-- ============================================================================

USE DB_BoticaBiofarma;
GO

-- 1. Agregar columna precio_venta si no existe
IF COL_LENGTH('Productos', 'precio_venta') IS NULL
BEGIN
    ALTER TABLE Productos
    ADD precio_venta DECIMAL(10,2) NOT NULL
        CONSTRAINT DF_Productos_PrecioVenta DEFAULT (0.00);
END
GO

-- 2. Agregar columna stock_actual si no existe
IF COL_LENGTH('Productos', 'stock_actual') IS NULL
BEGIN
    ALTER TABLE Productos
    ADD stock_actual INT NOT NULL
        CONSTRAINT DF_Productos_StockActual DEFAULT (0);
END
GO

-- 3. Agregar columna requiere_receta si no existe
IF COL_LENGTH('Productos', 'requiere_receta') IS NULL
BEGIN
    ALTER TABLE Productos
    ADD requiere_receta BIT NOT NULL
        CONSTRAINT DF_Productos_RequiereReceta DEFAULT (0);
END
GO
