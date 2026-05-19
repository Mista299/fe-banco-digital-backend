-- =============================================================
-- V2: Migración para soportar transferencias interbancarias (ACH)
-- Aplica sobre el esquema del Sprint 2 original (script-sprint2.sql).
-- Es segura: no borra datos, solo amplía el esquema.
-- =============================================================

-- -------------------------------------------------------------
-- 1. cliente — agregar columnas requeridas por la entidad JPA
-- -------------------------------------------------------------
ALTER TABLE cliente
    ADD COLUMN IF NOT EXISTS fecha_expedicion DATE,
    ADD COLUMN IF NOT EXISTS direccion        VARCHAR(255);

-- -------------------------------------------------------------
-- 2. cuenta — columna version para optimistic locking (@Version)
-- -------------------------------------------------------------
ALTER TABLE cuenta
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Ampliar CHECK de estado para incluir BLOQUEADA
ALTER TABLE cuenta DROP CONSTRAINT IF EXISTS cuenta_estado_check;
ALTER TABLE cuenta ADD CONSTRAINT cuenta_estado_check
    CHECK (estado IN ('ACTIVA', 'INACTIVA', 'BLOQUEADA'));

-- -------------------------------------------------------------
-- 3. usuario — ampliar CHECK de estado para incluir INACTIVO
-- -------------------------------------------------------------
ALTER TABLE usuario DROP CONSTRAINT IF EXISTS usuario_estado_check;
ALTER TABLE usuario ADD CONSTRAINT usuario_estado_check
    CHECK (estado IN ('ACTIVO', 'INACTIVO', 'BLOQUEADO'));

-- -------------------------------------------------------------
-- 4. transaccion — ampliar tipo y estado, agregar columnas ACH
-- -------------------------------------------------------------

-- Ampliar tamaño de columna tipo (TRANSFERENCIA_INTERBANCARIA = 26 chars)
ALTER TABLE transaccion ALTER COLUMN tipo TYPE VARCHAR(40);

-- Reemplazar CHECK de tipo con los cinco valores actuales
ALTER TABLE transaccion DROP CONSTRAINT IF EXISTS transaccion_tipo_check;
ALTER TABLE transaccion ADD CONSTRAINT transaccion_tipo_check
    CHECK (tipo IN (
        'DEPOSITO',
        'RETIRO',
        'TRANSFERENCIA',
        'TRANSFERENCIA_INTERBANCARIA',
        'REVERSO_ACH'
    ));

-- Ampliar tamaño de columna estado (PENDIENTE_PROCESAMIENTO = 24 chars)
ALTER TABLE transaccion ALTER COLUMN estado TYPE VARCHAR(30);

-- Reemplazar CHECK de estado con los cinco valores actuales
ALTER TABLE transaccion DROP CONSTRAINT IF EXISTS transaccion_estado_check;
ALTER TABLE transaccion ADD CONSTRAINT transaccion_estado_check
    CHECK (estado IN (
        'EXITOSA',
        'FALLIDA',
        'PENDIENTE_PROCESAMIENTO',
        'RECHAZADA',
        'REVERSADA'
    ));

-- Agregar columnas del flujo interbancario ACH
ALTER TABLE transaccion
    ADD COLUMN IF NOT EXISTS banco_destino                 VARCHAR(120),
    ADD COLUMN IF NOT EXISTS tipo_cuenta_destino_externa   VARCHAR(30),
    ADD COLUMN IF NOT EXISTS numero_cuenta_destino_externa VARCHAR(40),
    ADD COLUMN IF NOT EXISTS tipo_documento_receptor       VARCHAR(30),
    ADD COLUMN IF NOT EXISTS numero_documento_receptor     VARCHAR(40),
    ADD COLUMN IF NOT EXISTS nombre_receptor_externo       VARCHAR(150),
    ADD COLUMN IF NOT EXISTS referencia_externa            VARCHAR(80),
    ADD COLUMN IF NOT EXISTS motivo_rechazo                VARCHAR(255);
