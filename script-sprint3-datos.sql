-- =============================================================
--  Banco Digital — Script de DATOS (semilla)
--  Base de datos : banco2026  (PostgreSQL 16+)
--  Sprint        : 3
--  Generado      : 2026-05-29
-- =============================================================
--
--  Contiene SOLO los datos a insertar. El esquema, constraints,
--  triggers y permisos están en:  script-sprint3.sql
--
--  REQUISITO: ejecutar PRIMERO el script de esquema:
--    1) psql -v clave_mista='LA_CLAVE' -d banco2026 -f script-sprint3.sql
--    2) psql -d banco2026 -f script-sprint3-datos.sql   (este archivo)
--
--  Idempotente: todas las inserciones usan ON CONFLICT DO NOTHING,
--  así que re-ejecutar no duplica datos.
-- =============================================================


-- #############################################################
-- ##  SECCIÓN 1 — ROLES DEL SISTEMA                           ##
-- #############################################################
INSERT INTO rol (nombre) VALUES
    ('ADMIN'),
    ('GERENTE'),
    ('CLIENTE')
ON CONFLICT (nombre) DO NOTHING;


-- #############################################################
-- ##  SECCIÓN 2 — CLIENTES                                    ##
-- #############################################################
INSERT INTO cliente (nombre, documento, fecha_expedicion, email, direccion, telefono, genero) VALUES
    ('Ana Gómez',      '987654321', '2019-05-02', 'ana@example.com',    'Carrera 15 #8-45',      '3000000002', 'FEMENINO'),
    ('Carlos Pérez',   '111111111', '2021-07-19', 'carlos@example.com', 'Diagonal 50 #14-90',    '3000000003', 'MASCULINO'),
    ('Laura Martínez', '222222222', '2018-03-11', 'laura@example.com',  'Transversal 12 #45-67', '3000000004', 'FEMENINO'),
    ('Jorge Ramírez',  '333333333', '2017-09-30', 'jorge@example.com',  'Avenida 80 #30-12',     '3000000005', 'MASCULINO'),
    ('Sofía Vargas',   '444444444', '2022-06-15', 'sofia@example.com',  'Calle 5 #10-20',        '3000000006', 'FEMENINO'),
    ('Bryan Molina',   '123456789', '2020-01-10', 'bryan@example.com',  'Calle 10 #20-30',       '3000000001', 'MASCULINO')
ON CONFLICT (documento) DO NOTHING;


-- #############################################################
-- ##  SECCIÓN 3 — USUARIOS                                    ##
-- ##  Contraseñas BCrypt strength 10:                         ##
-- ##    ana/ana123  carlos/carlos123  laura/laura123          ##
-- ##    jorge/jorge123  sofia/sofia123                        ##
-- #############################################################
INSERT INTO usuario (username, password_hash, estado, id_cliente)
SELECT 'ana',    '$2a$10$QcSS89g4jkVBM1X3HCo15eLTXYOaxwuXAB.juNI7BETyqouPwgrXG', 'ACTIVO',    id_cliente FROM cliente WHERE documento = '987654321'
ON CONFLICT (username) DO NOTHING;
INSERT INTO usuario (username, password_hash, estado, id_cliente)
SELECT 'carlos', '$2a$10$0XT7WMtNLsIja/flwPhir.h8X1t8yUjYwqSlR2CVVp9dMjOpCplfq', 'ACTIVO',    id_cliente FROM cliente WHERE documento = '111111111'
ON CONFLICT (username) DO NOTHING;
INSERT INTO usuario (username, password_hash, estado, id_cliente)
SELECT 'laura',  '$2a$10$24Ea1uoKdu3koz6MqIQ63OF8/b0/RIdTaAU9DmwFz8EvqSo10u80y', 'ACTIVO',    id_cliente FROM cliente WHERE documento = '222222222'
ON CONFLICT (username) DO NOTHING;
INSERT INTO usuario (username, password_hash, estado, id_cliente)
SELECT 'jorge',  '$2a$10$n3mx4T/3tL2Feurfn7cZSuiYoSrK1UaU75puQMSy4wieF/dhQxSay', 'BLOQUEADO', id_cliente FROM cliente WHERE documento = '333333333'
ON CONFLICT (username) DO NOTHING;
INSERT INTO usuario (username, password_hash, estado, id_cliente)
SELECT 'sofia',  '$2a$10$YTTAQ5CpgzXEIqIH9a2RceBBZ4Rg8TR3mN5wBrcAabaWOkfJOc/gK', 'ACTIVO',    id_cliente FROM cliente WHERE documento = '444444444'
ON CONFLICT (username) DO NOTHING;

-- Rol CLIENTE para los usuarios anteriores
INSERT INTO usuario_rol (id_usuario, id_rol)
SELECT u.id_usuario, r.id_rol
FROM usuario u
CROSS JOIN rol r
WHERE u.username IN ('ana', 'carlos', 'laura', 'jorge', 'sofia')
  AND r.nombre = 'CLIENTE'
ON CONFLICT (id_usuario, id_rol) DO NOTHING;


-- #############################################################
-- ##  SECCIÓN 4 — CUENTAS BANCARIAS                           ##
-- #############################################################
INSERT INTO cuenta (numero_cuenta, tipo, estado, saldo, id_cliente, version)
SELECT '00020001', 'AHORROS',   'ACTIVA',   1225000.0000, id_cliente, 0 FROM cliente WHERE documento = '987654321'
ON CONFLICT (numero_cuenta) DO NOTHING;
INSERT INTO cuenta (numero_cuenta, tipo, estado, saldo, id_cliente, version)
SELECT '00030001', 'AHORROS',   'ACTIVA',         0.0000, id_cliente, 0 FROM cliente WHERE documento = '111111111'
ON CONFLICT (numero_cuenta) DO NOTHING;
INSERT INTO cuenta (numero_cuenta, tipo, estado, saldo, id_cliente, version)
SELECT '00040001', 'CORRIENTE', 'ACTIVA',   150000.0000,  id_cliente, 0 FROM cliente WHERE documento = '222222222'
ON CONFLICT (numero_cuenta) DO NOTHING;
INSERT INTO cuenta (numero_cuenta, tipo, estado, saldo, id_cliente, version)
SELECT '00050001', 'AHORROS',   'INACTIVA',       0.0000, id_cliente, 0 FROM cliente WHERE documento = '333333333'
ON CONFLICT (numero_cuenta) DO NOTHING;
INSERT INTO cuenta (numero_cuenta, tipo, estado, saldo, id_cliente, version)
SELECT '00060001', 'AHORROS',   'ACTIVA',   500000.0000,  id_cliente, 0 FROM cliente WHERE documento = '444444444'
ON CONFLICT (numero_cuenta) DO NOTHING;


-- #############################################################
-- ##  SECCIÓN 5 — USUARIO ADMINISTRADOR                       ##
-- ##  Contraseña BCrypt strength 10: bryan / bryan123         ##
-- #############################################################
INSERT INTO usuario (username, password_hash, estado, id_cliente)
SELECT 'bryan', '$2a$10$Pk8D8.TuVKew9nt8usnqKeHUrdpX.xsszkHKmT0WifJCwJ0Il9eHC', 'ACTIVO', id_cliente
FROM cliente WHERE documento = '123456789'
ON CONFLICT (username) DO NOTHING;

INSERT INTO cuenta (numero_cuenta, tipo, estado, saldo, id_cliente, version)
SELECT '00010001', 'AHORROS', 'ACTIVA', 530000.0000, id_cliente, 0
FROM cliente WHERE documento = '123456789'
ON CONFLICT (numero_cuenta) DO NOTHING;

INSERT INTO usuario_rol (id_usuario, id_rol)
SELECT u.id_usuario, r.id_rol
FROM usuario u
CROSS JOIN rol r
WHERE u.username = 'bryan'
  AND r.nombre = 'ADMIN'
ON CONFLICT (id_usuario, id_rol) DO NOTHING;


-- #############################################################
-- ##  SECCIÓN 6 — ASIENTOS DE APERTURA (libro mayor)          ##
-- ##  Idempotente.                                            ##
-- ##  Las cuentas creadas por la app nacen con saldo 0 y todo ##
-- ##  cambio posterior se registra por trigger (UPDATE). Las  ##
-- ##  cuentas semilla que nacen con saldo > 0 necesitan su    ##
-- ##  asiento de apertura; se inserta aquí solo si la cuenta  ##
-- ##  aún no tiene asientos, de modo que re-ejecutar no       ##
-- ##  duplica nada.                                           ##
-- #############################################################
INSERT INTO libro_mayor (id_cuenta, saldo_anterior, monto, tipo_asiento, saldo_resultante)
SELECT c.id_cuenta, 0, c.saldo, 'APERTURA', c.saldo
FROM cuenta c
WHERE c.saldo > 0
  AND NOT EXISTS (SELECT 1 FROM libro_mayor lm WHERE lm.id_cuenta = c.id_cuenta);


-- =============================================================
--  FIN DEL SCRIPT DE DATOS
--  Verificación rápida:
--    SELECT * FROM v_reconciliacion_saldos;
--    SELECT * FROM fn_verificar_integridad();   -- vacío = todo OK
-- =============================================================
