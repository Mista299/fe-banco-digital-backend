-- =========================
-- 1. ROLES
-- =========================
INSERT INTO rol (id, nombre)
VALUES 
(1, 'ADMIN'),
(2, 'CLIENTE')
ON CONFLICT DO NOTHING;

-- =========================
-- 2. CLIENTES
-- =========================
INSERT INTO cliente (id, nombre, documento, fecha_expedicion, email, direccion, telefono)
VALUES 
(1, 'Bryan Molina', '123456789', '2020-01-10', 'bryan@example.com', 'Calle 10 #20-30', '3000000001'),
(2, 'Ana Gómez', '987654321', '2019-05-02', 'ana@example.com', 'Carrera 15 #8-45', '3000000002'),
(3, 'Carlos Pérez', '111111111', '2021-07-19', 'carlos@example.com', 'Diagonal 50 #14-90', '3000000003'),
(4, 'Laura Martínez', '222222222', '2018-03-11', 'laura@example.com', 'Transversal 12 #45-67', '3000000004'),
(5, 'Jorge Ramírez', '333333333', '2017-09-30', 'jorge@example.com', 'Avenida 80 #30-12', '3000000005'),
(6, 'Sofía Vargas', '444444444', '2022-06-15', 'sofia@example.com', 'Calle 5 #10-20', '3000000006')
ON CONFLICT DO NOTHING;

-- =========================
-- 3. USUARIOS (passwords solo de prueba)
-- =========================
INSERT INTO usuario (id, username, password_hash, estado, cliente_id)
VALUES
(1, 'bryan', 'bryan123', 'ACTIVO', 1),
(2, 'ana', 'ana123', 'ACTIVO', 2),
(3, 'carlos', 'carlos123', 'ACTIVO', 3),
(4, 'laura', 'laura123', 'ACTIVO', 4),
(5, 'jorge', 'jorge123', 'BLOQUEADO', 5)
ON CONFLICT DO NOTHING;

-- =========================
-- 4. CUENTAS
-- =========================
INSERT INTO cuenta (id, numero_cuenta, tipo, estado, saldo, cliente_id)
VALUES
(1, '00010001', 'AHORROS', 'ACTIVA', 850000.00, 1),
(2, '00020001', 'AHORROS', 'ACTIVA', 1200000.00, 2),
(3, '00030001', 'AHORROS', 'ACTIVA', 0.00, 3),
(4, '00040001', 'CORRIENTE', 'ACTIVA', 50000.00, 4),
(5, '00050001', 'AHORROS', 'INACTIVA', 0.00, 5)
ON CONFLICT DO NOTHING;

-- =========================
-- 5. TRANSACCIONES
-- =========================
INSERT INTO transaccion (id, tipo, estado, monto, cuenta_origen_id, cuenta_destino_id)
VALUES
(1, 'DEPOSITO', 'EXITOSA', 850000.00, NULL, 1),
(2, 'DEPOSITO', 'EXITOSA', 1200000.00, NULL, 2),
(3, 'TRANSFERENCIA', 'EXITOSA', 50000.00, 1, 4),
(4, 'RETIRO', 'EXITOSA', 200000.00, 2, NULL),
(5, 'DEPOSITO', 'EXITOSA', 100000.00, NULL, 4)
ON CONFLICT DO NOTHING;

-- =========================
-- 6. AUDITORÍA
-- =========================
INSERT INTO auditoria (id, accion, detalle, usuario_id)
VALUES
(1, 'LOGIN', 'Inicio de sesión exitoso de bryan', 1),
(2, 'CONSULTA_PERFIL', 'Consulta de perfil de cliente', 2),
(3, 'CIERRE_CUENTA', 'Cierre exitoso de la cuenta 00030001', 3),
(4, 'INTENTO_CIERRE', 'Intento de cierre rechazado por saldo pendiente', 4),
(5, 'BLOQUEO_USUARIO', 'Usuario bloqueado para pruebas de seguridad', 5)
ON CONFLICT DO NOTHING;