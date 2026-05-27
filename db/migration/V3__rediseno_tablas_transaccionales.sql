-- =============================================================
-- V3: Reemplazo de la tabla transaccion por cuatro tablas
--     especializadas con integridad referencial completa.
-- Aplica en entornos con DDL_AUTO=update/validate o Flyway.
-- Con DDL_AUTO=create JPA recrea todo y este script no corre.
-- =============================================================

-- -------------------------------------------------------------
-- 1. Crear las cuatro tablas nuevas
-- -------------------------------------------------------------

CREATE TABLE IF NOT EXISTS movimiento (
    id_movimiento  BIGSERIAL      PRIMARY KEY,
    id_cuenta      BIGINT         NOT NULL REFERENCES cuenta(id_cuenta),
    tipo           VARCHAR(20)    NOT NULL CHECK (tipo IN ('DEPOSITO','RETIRO')),
    monto          DECIMAL(19,4)  NOT NULL CHECK (monto > 0),
    fecha          TIMESTAMP      NOT NULL DEFAULT NOW(),
    estado         VARCHAR(20)    NOT NULL CHECK (estado IN ('EXITOSO','FALLIDO')),
    descripcion    VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_movimiento_cuenta ON movimiento(id_cuenta);

CREATE TABLE IF NOT EXISTS transferencia (
    id_transferencia   BIGSERIAL      PRIMARY KEY,
    id_cuenta_origen   BIGINT         NOT NULL REFERENCES cuenta(id_cuenta),
    id_cuenta_destino  BIGINT         NOT NULL REFERENCES cuenta(id_cuenta),
    monto              DECIMAL(19,4)  NOT NULL CHECK (monto > 0),
    fecha              TIMESTAMP      NOT NULL DEFAULT NOW(),
    estado             VARCHAR(20)    NOT NULL CHECK (estado IN ('EXITOSA','FALLIDA'))
);
CREATE INDEX IF NOT EXISTS idx_transferencia_origen  ON transferencia(id_cuenta_origen);
CREATE INDEX IF NOT EXISTS idx_transferencia_destino ON transferencia(id_cuenta_destino);

CREATE TABLE IF NOT EXISTS transferencia_externa (
    id_transf_ext              BIGSERIAL      PRIMARY KEY,
    id_cuenta_origen           BIGINT         NOT NULL REFERENCES cuenta(id_cuenta),
    banco_destino              VARCHAR(120)   NOT NULL,
    tipo_cuenta_destino        VARCHAR(30)    NOT NULL,
    numero_cuenta_destino      VARCHAR(40)    NOT NULL,
    tipo_documento_receptor    VARCHAR(30)    NOT NULL,
    numero_documento_receptor  VARCHAR(40)    NOT NULL,
    nombre_receptor            VARCHAR(150)   NOT NULL,
    monto                      DECIMAL(19,4)  NOT NULL CHECK (monto > 0),
    fecha                      TIMESTAMP      NOT NULL DEFAULT NOW(),
    estado                     VARCHAR(30)    NOT NULL
        CHECK (estado IN ('PENDIENTE_PROCESAMIENTO','EXITOSA','RECHAZADA','REVERSADA')),
    referencia_externa         VARCHAR(80),
    motivo_rechazo             VARCHAR(255),
    id_transf_original         BIGINT         REFERENCES transferencia_externa(id_transf_ext)
);
CREATE INDEX IF NOT EXISTS idx_transf_ext_origen ON transferencia_externa(id_cuenta_origen);

CREATE TABLE IF NOT EXISTS transferencia_internacional (
    id_transf_int              BIGSERIAL      PRIMARY KEY,
    id_cuenta_origen           BIGINT         NOT NULL REFERENCES cuenta(id_cuenta),
    banco_destino              VARCHAR(150)   NOT NULL,
    codigo_swift               VARCHAR(11)    NOT NULL,
    pais_destino               VARCHAR(60)    NOT NULL,
    tipo_cuenta_destino        VARCHAR(30)    NOT NULL,
    iban_cuenta_destino        VARCHAR(34)    NOT NULL,
    tipo_documento_receptor    VARCHAR(30)    NOT NULL,
    numero_documento_receptor  VARCHAR(40)    NOT NULL,
    nombre_receptor            VARCHAR(150)   NOT NULL,
    monto_usd                  DECIMAL(19,4)  NOT NULL CHECK (monto_usd > 0),
    tasa_cambio                DECIMAL(19,6)  NOT NULL CHECK (tasa_cambio > 0),
    monto_cop                  DECIMAL(19,4)  NOT NULL,
    moneda                     VARCHAR(3)     NOT NULL DEFAULT 'USD',
    fecha                      TIMESTAMP      NOT NULL DEFAULT NOW(),
    estado                     VARCHAR(30)    NOT NULL
        CHECK (estado IN ('PENDIENTE_PROCESAMIENTO','EXITOSA','RECHAZADA','REVERSADA')),
    referencia_swift           VARCHAR(80),
    motivo_rechazo             VARCHAR(255)
);
CREATE INDEX IF NOT EXISTS idx_transf_int_origen ON transferencia_internacional(id_cuenta_origen);

-- -------------------------------------------------------------
-- 2. Migrar datos existentes desde transaccion
-- -------------------------------------------------------------

-- Depósitos: cuentaDestino recibe
INSERT INTO movimiento (id_cuenta, tipo, monto, fecha, estado)
    SELECT id_cuenta_destino, 'DEPOSITO', monto, fecha,
           CASE estado WHEN 'EXITOSA' THEN 'EXITOSO' ELSE 'FALLIDO' END
    FROM transaccion
    WHERE tipo = 'DEPOSITO' AND id_cuenta_destino IS NOT NULL;

-- Retiros: cuentaOrigen debita
INSERT INTO movimiento (id_cuenta, tipo, monto, fecha, estado)
    SELECT id_cuenta_origen, 'RETIRO', monto, fecha,
           CASE estado WHEN 'EXITOSA' THEN 'EXITOSO' ELSE 'FALLIDO' END
    FROM transaccion
    WHERE tipo = 'RETIRO' AND id_cuenta_origen IS NOT NULL;

-- Reversos ACH: devolución como depósito en cuentaDestino
INSERT INTO movimiento (id_cuenta, tipo, monto, fecha, estado, descripcion)
    SELECT id_cuenta_destino, 'DEPOSITO', monto, fecha, 'EXITOSO',
           'Reversión ACH: ' || COALESCE(referencia_externa, '')
    FROM transaccion
    WHERE tipo = 'REVERSO_ACH' AND id_cuenta_destino IS NOT NULL;

-- Transferencias internas
INSERT INTO transferencia (id_cuenta_origen, id_cuenta_destino, monto, fecha, estado)
    SELECT id_cuenta_origen, id_cuenta_destino, monto, fecha,
           CASE estado WHEN 'EXITOSA' THEN 'EXITOSA' ELSE 'FALLIDA' END
    FROM transaccion
    WHERE tipo = 'TRANSFERENCIA'
      AND id_cuenta_origen IS NOT NULL
      AND id_cuenta_destino IS NOT NULL;

-- Transferencias interbancarias ACH
INSERT INTO transferencia_externa (
    id_cuenta_origen, banco_destino, tipo_cuenta_destino, numero_cuenta_destino,
    tipo_documento_receptor, numero_documento_receptor, nombre_receptor,
    monto, fecha, estado, referencia_externa, motivo_rechazo)
    SELECT id_cuenta_origen,
           COALESCE(banco_destino, ''),
           COALESCE(tipo_cuenta_destino_externa, ''),
           COALESCE(numero_cuenta_destino_externa, ''),
           COALESCE(tipo_documento_receptor, ''),
           COALESCE(numero_documento_receptor, ''),
           COALESCE(nombre_receptor_externo, ''),
           monto, fecha, estado, referencia_externa, motivo_rechazo
    FROM transaccion
    WHERE tipo = 'TRANSFERENCIA_INTERBANCARIA'
      AND id_cuenta_origen IS NOT NULL;

-- -------------------------------------------------------------
-- 3. Eliminar tabla vieja
-- -------------------------------------------------------------

DROP TABLE IF EXISTS transaccion;
