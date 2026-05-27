-- =============================================================
-- V4: Tablas para retiro con token y depósito pendiente
--     (flujo de pasarela de pagos con validación HMAC)
-- =============================================================

CREATE TABLE IF NOT EXISTS token_retiro (
    id_token         BIGSERIAL       PRIMARY KEY,
    codigo           VARCHAR(6)      NOT NULL UNIQUE,
    monto            DECIMAL(19,4)   NOT NULL CHECK (monto > 0),
    fecha_expiracion TIMESTAMP       NOT NULL,
    fecha_creacion   TIMESTAMP       NOT NULL DEFAULT NOW(),
    estado           VARCHAR(10)     NOT NULL CHECK (estado IN ('ACTIVO','USADO','EXPIRADO')),
    id_cuenta        BIGINT          NOT NULL REFERENCES cuenta(id_cuenta)
);
CREATE INDEX IF NOT EXISTS idx_token_retiro_codigo ON token_retiro(codigo);

CREATE TABLE IF NOT EXISTS deposito_pendiente (
    id                  BIGSERIAL       PRIMARY KEY,
    referencia_gateway  VARCHAR(255)    NOT NULL UNIQUE,
    numero_cuenta       VARCHAR(20)     NOT NULL,
    monto               DECIMAL(19,2)   NOT NULL CHECK (monto > 0),
    estado              VARCHAR(20)     NOT NULL CHECK (estado IN ('PENDIENTE','COMPLETADO','EXPIRADO')),
    fecha_creacion      TIMESTAMP       NOT NULL,
    fecha_expiracion    TIMESTAMP       NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_deposito_pendiente_ref ON deposito_pendiente(referencia_gateway);
