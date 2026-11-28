-- ============================================================================
-- SCRIPT: 06_indices_busqueda_hu05.sql
-- PROYECTO: Botica Biofarma
-- HISTORIA DE USUARIO: HU-05 - Busqueda y consulta de disponibilidad y stock
-- OBJETIVO: Optimizar las consultas de busqueda de medicamentos activos por
--           nombre comercial, principio activo y codigo evitando lecturas
--           completas de tabla (RNF-02: respuesta <= 2 segundos).
-- ============================================================================

USE DB_BoticaBiofarma;
GO

IF OBJECT_ID(N'dbo.Productos', N'U') IS NOT NULL
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = N'IX_Productos_Activo_NombreComercial'
          AND object_id = OBJECT_ID(N'dbo.Productos')
    )
    BEGIN
        CREATE NONCLUSTERED INDEX IX_Productos_Activo_NombreComercial
            ON dbo.Productos (activo, nombre_comercial)
            INCLUDE (
                codigo_producto,
                principio_activo,
                presentacion,
                laboratorio,
                precio_venta,
                stock_actual,
                requiere_receta
            );
    END

    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = N'IX_Productos_Activo_PrincipioActivo'
          AND object_id = OBJECT_ID(N'dbo.Productos')
    )
    BEGIN
        CREATE NONCLUSTERED INDEX IX_Productos_Activo_PrincipioActivo
            ON dbo.Productos (activo, principio_activo)
            INCLUDE (codigo_producto, nombre_comercial);
    END
END
GO
