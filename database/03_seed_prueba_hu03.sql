USE DB_BoticaBiofarma;
GO

-- =============================================================================
-- Script de datos de prueba (Seed) para validar HU-03 en SQL Server
-- =============================================================================

-- 1. Asegurar roles base
IF NOT EXISTS (SELECT 1 FROM Roles WHERE nombre = 'ADMINISTRADORA')
    INSERT INTO Roles (nombre) VALUES ('ADMINISTRADORA');

IF NOT EXISTS (SELECT 1 FROM Roles WHERE nombre = 'TRABAJADORA')
    INSERT INTO Roles (nombre) VALUES ('TRABAJADORA');
GO

DECLARE @idRolAdmin INT = (SELECT id_rol FROM Roles WHERE nombre = 'ADMINISTRADORA');
DECLARE @idRolTrab INT = (SELECT id_rol FROM Roles WHERE nombre = 'TRABAJADORA');

-- 2. Usuarios de prueba con contraseñas BCrypt reales
--    admin  -> Admin123*
--    trab01 -> Clave123*
--    trab02 -> Clave123*
IF NOT EXISTS (SELECT 1 FROM Usuarios WHERE codigo_usuario = 'admin')
BEGIN
    INSERT INTO Usuarios (codigo_usuario, nombres, apellidos, password_hash, id_rol, activo)
    VALUES ('admin', 'Propietaria', 'Biofarma', '$2a$10$vL5PJndpM5hUT2HzMSSUIO3Q2p4z5n73p6lbN0DO.K5.lkPSKxrk.', @idRolAdmin, 1);
END
ELSE
BEGIN
    UPDATE Usuarios
    SET password_hash = '$2a$10$vL5PJndpM5hUT2HzMSSUIO3Q2p4z5n73p6lbN0DO.K5.lkPSKxrk.'
    WHERE codigo_usuario = 'admin' AND (password_hash = '' OR password_hash IS NULL);
END

IF NOT EXISTS (SELECT 1 FROM Usuarios WHERE codigo_usuario = 'trab01')
BEGIN
    INSERT INTO Usuarios (codigo_usuario, nombres, apellidos, password_hash, id_rol, activo)
    VALUES ('trab01', 'Lucia', 'Gomez', '$2a$10$wyyJvJCLo2q2wGsk0ur0Suspb8NSB7u82sRvZcAWtKAWjQpwzNWGa', @idRolTrab, 1);
END

IF NOT EXISTS (SELECT 1 FROM Usuarios WHERE codigo_usuario = 'trab02')
BEGIN
    INSERT INTO Usuarios (codigo_usuario, nombres, apellidos, password_hash, id_rol, activo)
    VALUES ('trab02', 'Rosa', 'Paredes', '$2a$10$wyyJvJCLo2q2wGsk0ur0Suspb8NSB7u82sRvZcAWtKAWjQpwzNWGa', @idRolTrab, 1);
END
GO

-- 3. Productos y Lotes mínimos para respetar FK_DetalleVentas_Lotes
IF NOT EXISTS (SELECT 1 FROM Productos WHERE codigo_producto = 'MED-001')
BEGIN
    INSERT INTO Productos (codigo_producto, nombre_comercial, principio_activo, presentacion, laboratorio, stock_minimo)
    VALUES ('MED-001', 'Paracetamol 500mg', 'Paracetamol', 'Caja x 20 tabletas', 'Genfar', 10);
END

IF NOT EXISTS (SELECT 1 FROM Productos WHERE codigo_producto = 'MED-002')
BEGIN
    INSERT INTO Productos (codigo_producto, nombre_comercial, principio_activo, presentacion, laboratorio, stock_minimo)
    VALUES ('MED-002', 'Amoxicilina 500mg', 'Amoxicilina', 'Caja x 12 cápsulas', 'Portugal', 10);
END
GO

DECLARE @idProd1 INT = (SELECT id_producto FROM Productos WHERE codigo_producto = 'MED-001');
DECLARE @idProd2 INT = (SELECT id_producto FROM Productos WHERE codigo_producto = 'MED-002');

IF NOT EXISTS (SELECT 1 FROM Lotes WHERE numero_lote = 'LOT-2026-A')
BEGIN
    INSERT INTO Lotes (id_producto, numero_lote, registro_sanitario, fecha_vencimiento, cantidad_inicial, cantidad_actual, precio_compra, precio_venta)
    VALUES (@idProd1, 'LOT-2026-A', 'RS-EN00123', '2027-08-31', 100, 95, 8.00, 12.50);
END

IF NOT EXISTS (SELECT 1 FROM Lotes WHERE numero_lote = 'LOT-2026-B')
BEGIN
    INSERT INTO Lotes (id_producto, numero_lote, registro_sanitario, fecha_vencimiento, cantidad_inicial, cantidad_actual, precio_compra, precio_venta)
    VALUES (@idProd2, 'LOT-2026-B', 'RS-EN00456', '2027-10-31', 80, 76, 11.00, 16.00);
END
GO

-- 4. Ventas de prueba distribuidas entre Trabajadora A (trab01) y Trabajadora B (trab02)
DECLARE @idTrabA INT = (SELECT id_usuario FROM Usuarios WHERE codigo_usuario = 'trab01');
DECLARE @idTrabB INT = (SELECT id_usuario FROM Usuarios WHERE codigo_usuario = 'trab02');
DECLARE @idLoteA INT = (SELECT id_lote FROM Lotes WHERE numero_lote = 'LOT-2026-A');
DECLARE @idLoteB INT = (SELECT id_lote FROM Lotes WHERE numero_lote = 'LOT-2026-B');

IF NOT EXISTS (SELECT 1 FROM Ventas WHERE numero_ticket = 'T-2026-0001')
BEGIN
    INSERT INTO Ventas (numero_ticket, id_usuario, fecha_venta, total, estado)
    VALUES ('T-2026-0001', @idTrabA, DATEADD(HOUR, -3, SYSDATETIME()), 25.00, 'COMPLETADA');

    DECLARE @v1 INT = SCOPE_IDENTITY();
    INSERT INTO DetalleVentas (id_venta, id_lote, nombre_producto, numero_lote, cantidad, precio_unitario, subtotal)
    VALUES (@v1, @idLoteA, 'Paracetamol 500mg', 'LOT-2026-A', 2, 12.50, 25.00);
END

IF NOT EXISTS (SELECT 1 FROM Ventas WHERE numero_ticket = 'T-2026-0002')
BEGIN
    INSERT INTO Ventas (numero_ticket, id_usuario, fecha_venta, total, estado)
    VALUES ('T-2026-0002', @idTrabA, DATEADD(HOUR, -2, SYSDATETIME()), 44.50, 'COMPLETADA');

    DECLARE @v2 INT = SCOPE_IDENTITY();
    INSERT INTO DetalleVentas (id_venta, id_lote, nombre_producto, numero_lote, cantidad, precio_unitario, subtotal)
    VALUES
        (@v2, @idLoteA, 'Paracetamol 500mg', 'LOT-2026-A', 1, 12.50, 12.50),
        (@v2, @idLoteB, 'Amoxicilina 500mg', 'LOT-2026-B', 2, 16.00, 32.00);
END

IF NOT EXISTS (SELECT 1 FROM Ventas WHERE numero_ticket = 'T-2026-0003')
BEGIN
    INSERT INTO Ventas (numero_ticket, id_usuario, fecha_venta, total, estado)
    VALUES ('T-2026-0003', @idTrabB, DATEADD(HOUR, -1, SYSDATETIME()), 32.00, 'COMPLETADA');

    DECLARE @v3 INT = SCOPE_IDENTITY();
    INSERT INTO DetalleVentas (id_venta, id_lote, nombre_producto, numero_lote, cantidad, precio_unitario, subtotal)
    VALUES (@v3, @idLoteB, 'Amoxicilina 500mg', 'LOT-2026-B', 2, 16.00, 32.00);
END

IF NOT EXISTS (SELECT 1 FROM Ventas WHERE numero_ticket = 'T-2026-0004')
BEGIN
    INSERT INTO Ventas (numero_ticket, id_usuario, fecha_venta, total, estado)
    VALUES ('T-2026-0004', @idTrabB, SYSDATETIME(), 12.50, 'COMPLETADA');

    DECLARE @v4 INT = SCOPE_IDENTITY();
    INSERT INTO DetalleVentas (id_venta, id_lote, nombre_producto, numero_lote, cantidad, precio_unitario, subtotal)
    VALUES (@v4, @idLoteA, 'Paracetamol 500mg', 'LOT-2026-A', 1, 12.50, 12.50);
END
GO
