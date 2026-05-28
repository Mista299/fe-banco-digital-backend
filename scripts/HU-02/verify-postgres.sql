-- ================================================================
-- HU-02 · Verificación en PostgreSQL
-- Apertura automática de cuenta de ahorros al registrar usuario
--
-- Uso:
--   psql -U mista -d banco2026 -f scripts/HU-02/verify-postgres.sql
-- ================================================================

\echo ''
\echo '========================================================'
\echo '  HU-02 · Verificación en base de datos'
\echo '========================================================'

-- ── 1. Últimos 5 clientes registrados ────────────────────────────────────────
\echo ''
\echo '--- Últimos 5 clientes registrados ---'
SELECT
    cl.id_cliente,
    cl.nombre,
    cl.documento,
    cl.email,
    cl.fecha_registro
FROM cliente cl
ORDER BY cl.id_cliente DESC
LIMIT 5;

-- ── 2. Cuenta asignada a cada uno de esos clientes ───────────────────────────
\echo ''
\echo '--- Cuentas asignadas a los últimos 5 clientes ---'
SELECT
    cu.id_cuenta,
    cu.numero_cuenta,
    cu.tipo,
    cu.estado,
    cu.saldo,
    cu.saldo_disponible,
    cu.saldo_reservado,
    cl.nombre        AS cliente,
    cl.documento
FROM cuenta cu
JOIN cliente cl ON cl.id_cliente = cu.id_cliente
ORDER BY cu.id_cuenta DESC
LIMIT 5;

-- ── 3. Validaciones de la HU ─────────────────────────────────────────────────
\echo ''
\echo '--- Validaciones HU-02 ---'
SELECT
    cu.numero_cuenta,
    -- Escenario 1a: prefijo 500
    CASE WHEN cu.numero_cuenta LIKE '500%'
         THEN 'PASS' ELSE 'FAIL' END                         AS prefijo_500,
    -- Escenario 1b: longitud 10 dígitos
    CASE WHEN length(cu.numero_cuenta) = 10
         THEN 'PASS' ELSE 'FAIL' END                         AS longitud_10,
    -- Escenario 1c: tipo AHORROS
    CASE WHEN cu.tipo = 'AHORROS'
         THEN 'PASS' ELSE 'FAIL' END                         AS tipo_ahorros,
    -- Escenario 1d: saldo inicial 0
    CASE WHEN cu.saldo = 0
         THEN 'PASS' ELSE 'FAIL' END                         AS saldo_cero,
    -- Escenario 2: estado ACTIVA
    CASE WHEN cu.estado = 'ACTIVA'
         THEN 'PASS' ELSE 'FAIL' END                         AS estado_activa,
    -- Integridad: saldo_disponible = saldo en cuenta nueva
    CASE WHEN cu.saldo_disponible = cu.saldo
         THEN 'PASS' ELSE 'FAIL' END                         AS saldo_disponible_ok,
    -- Integridad: saldo_reservado = 0 en cuenta nueva
    CASE WHEN cu.saldo_reservado = 0
         THEN 'PASS' ELSE 'FAIL' END                         AS reservado_cero,
    cl.nombre                                                 AS cliente
FROM cuenta cu
JOIN cliente cl ON cl.id_cliente = cu.id_cliente
ORDER BY cu.id_cuenta DESC
LIMIT 5;

-- ── 4. Verificar unicidad del número de cuenta ───────────────────────────────
\echo ''
\echo '--- Números de cuenta duplicados (debe estar vacío) ---'
SELECT
    numero_cuenta,
    COUNT(*) AS repeticiones
FROM cuenta
GROUP BY numero_cuenta
HAVING COUNT(*) > 1;

-- ── 5. Cuentas sin prefijo 500 de tipo AHORROS (anomalías) ──────────────────
\echo ''
\echo '--- Cuentas AHORROS con número fuera de formato (debe estar vacío) ---'
SELECT id_cuenta, numero_cuenta, tipo, estado
FROM cuenta
WHERE tipo = 'AHORROS'
  AND (numero_cuenta NOT LIKE '500%' OR length(numero_cuenta) <> 10);

-- ── 6. Resumen de cuentas por tipo y estado ──────────────────────────────────
\echo ''
\echo '--- Resumen general: cuentas por tipo y estado ---'
SELECT
    tipo,
    estado,
    COUNT(*)    AS total,
    SUM(saldo)  AS saldo_total
FROM cuenta
GROUP BY tipo, estado
ORDER BY tipo, estado;

\echo ''
\echo '========================================================'
\echo '  Verificación completada'
\echo '========================================================'
