-- =============================================================
-- Banco Digital — Script SQL completo
-- Base de datos: banco2026 (PostgreSQL)
-- Sincronizado con entidades JPA actuales (Spring Boot / Hibernate)
-- =============================================================

-- -------------------------------------------------------------
-- Limpieza (orden inverso a FK)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS usuario_rol              CASCADE;
DROP TABLE IF EXISTS auditoria                CASCADE;
DROP TABLE IF EXISTS refresh_token            CASCADE;
DROP TABLE IF EXISTS movimiento               CASCADE;
DROP TABLE IF EXISTS transferencia            CASCADE;
DROP TABLE IF EXISTS transferencia_externa    CASCADE;
DROP TABLE IF EXISTS transferencia_internacional CASCADE;
DROP TABLE IF EXISTS cuenta                   CASCADE;
DROP TABLE IF EXISTS usuario                  CASCADE;
DROP TABLE IF EXISTS rol                      CASCADE;
DROP TABLE IF EXISTS cliente                  CASCADE;

-- -------------------------------------------------------------
-- cliente
-- -------------------------------------------------------------
CREATE TABLE cliente (
    id_cliente       BIGSERIAL      PRIMARY KEY,
    nombre           VARCHAR(255)   NOT NULL,
    documento        VARCHAR(255)   NOT NULL UNIQUE,
    fecha_expedicion DATE           NOT NULL,
    email            VARCHAR(255)   NOT NULL UNIQUE,
    direccion        VARCHAR(255)   NOT NULL,
    telefono         VARCHAR(255),
    genero           VARCHAR(255)   NOT NULL CHECK (genero IN ('MASCULINO', 'FEMENINO')),
    fecha_registro   TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- rol
-- -------------------------------------------------------------
CREATE TABLE rol (
    id_rol BIGSERIAL   PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL UNIQUE CHECK (nombre IN ('ADMIN', 'CLIENTE'))
);

-- -------------------------------------------------------------
-- usuario
-- -------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario    BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    estado        VARCHAR(255) NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO', 'BLOQUEADO')),
    id_cliente    BIGINT       NOT NULL UNIQUE REFERENCES cliente(id_cliente)
);

-- -------------------------------------------------------------
-- usuario_rol
-- -------------------------------------------------------------
CREATE TABLE usuario_rol (
    id_usuario BIGINT NOT NULL REFERENCES usuario(id_usuario),
    id_rol     BIGINT NOT NULL REFERENCES rol(id_rol),
    PRIMARY KEY (id_usuario, id_rol)
);

-- -------------------------------------------------------------
-- cuenta
-- Nota: columna `version` requerida por @Version de Hibernate
-- -------------------------------------------------------------
CREATE TABLE cuenta (
    id_cuenta     BIGSERIAL     PRIMARY KEY,
    version       BIGINT        NOT NULL DEFAULT 0,
    numero_cuenta VARCHAR(255)  NOT NULL UNIQUE,
    tipo          VARCHAR(255)  NOT NULL CHECK (tipo IN ('AHORROS', 'CORRIENTE')),
    saldo         DECIMAL(19,4),
    estado        VARCHAR(255)  NOT NULL
                                CHECK (estado IN ('ACTIVA', 'INACTIVA', 'BLOQUEADA', 'PENDIENTE_APROBACION')),
    id_cliente    BIGINT        NOT NULL REFERENCES cliente(id_cliente)
);

-- -------------------------------------------------------------
-- movimiento  (depósitos y retiros)
-- -------------------------------------------------------------
CREATE TABLE movimiento (
    id_movimiento BIGSERIAL     PRIMARY KEY,
    id_cuenta     BIGINT        NOT NULL REFERENCES cuenta(id_cuenta),
    tipo          VARCHAR(255)  NOT NULL CHECK (tipo IN ('DEPOSITO', 'RETIRO')),
    monto         DECIMAL(19,4) NOT NULL,
    fecha         TIMESTAMP     NOT NULL DEFAULT NOW(),
    estado        VARCHAR(255)  NOT NULL CHECK (estado IN ('EXITOSO', 'FALLIDO')),
    descripcion   VARCHAR(255)
);

-- -------------------------------------------------------------
-- transferencia  (interna entre cuentas propias/ajenas)
-- -------------------------------------------------------------
CREATE TABLE transferencia (
    id_transferencia BIGSERIAL     PRIMARY KEY,
    id_cuenta_origen  BIGINT       NOT NULL REFERENCES cuenta(id_cuenta),
    id_cuenta_destino BIGINT       NOT NULL REFERENCES cuenta(id_cuenta),
    monto             DECIMAL(19,4) NOT NULL,
    fecha             TIMESTAMP    NOT NULL DEFAULT NOW(),
    estado            VARCHAR(255) NOT NULL CHECK (estado IN ('EXITOSA', 'FALLIDA'))
);

-- -------------------------------------------------------------
-- transferencia_externa  (ACH / interbancaria)
-- -------------------------------------------------------------
CREATE TABLE transferencia_externa (
    id_transf_ext              BIGSERIAL      PRIMARY KEY,
    id_cuenta_origen           BIGINT         NOT NULL REFERENCES cuenta(id_cuenta),
    banco_destino              VARCHAR(120)   NOT NULL,
    tipo_cuenta_destino        VARCHAR(30)    NOT NULL,
    numero_cuenta_destino      VARCHAR(40)    NOT NULL,
    tipo_documento_receptor    VARCHAR(30)    NOT NULL,
    numero_documento_receptor  VARCHAR(40)    NOT NULL,
    nombre_receptor            VARCHAR(150)   NOT NULL,
    monto                      DECIMAL(19,4)  NOT NULL,
    fecha                      TIMESTAMP      NOT NULL DEFAULT NOW(),
    estado                     VARCHAR(30)    NOT NULL
                                              CHECK (estado IN ('PENDIENTE_PROCESAMIENTO', 'EXITOSA', 'RECHAZADA', 'REVERSADA')),
    referencia_externa         VARCHAR(80),
    motivo_rechazo             VARCHAR(255),
    id_transf_original         BIGINT         REFERENCES transferencia_externa(id_transf_ext)
);

-- -------------------------------------------------------------
-- transferencia_internacional  (SWIFT)
-- -------------------------------------------------------------
CREATE TABLE transferencia_internacional (
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
    monto_usd                  DECIMAL(19,4)  NOT NULL,
    tasa_cambio                DECIMAL(19,6)  NOT NULL,
    monto_cop                  DECIMAL(19,4)  NOT NULL,
    moneda                     VARCHAR(3)     NOT NULL DEFAULT 'USD',
    fecha                      TIMESTAMP      NOT NULL DEFAULT NOW(),
    estado                     VARCHAR(30)    NOT NULL
                                              CHECK (estado IN ('PENDIENTE_PROCESAMIENTO', 'EXITOSA', 'RECHAZADA', 'REVERSADA')),
    referencia_swift           VARCHAR(80),
    motivo_rechazo             VARCHAR(255)
);

-- -------------------------------------------------------------
-- auditoria
-- -------------------------------------------------------------
CREATE TABLE auditoria (
    id_auditoria BIGSERIAL    PRIMARY KEY,
    accion       VARCHAR(255) NOT NULL,
    id_usuario   BIGINT       NOT NULL REFERENCES usuario(id_usuario),
    fecha        TIMESTAMP    NOT NULL DEFAULT NOW(),
    detalle      TEXT
);

-- -------------------------------------------------------------
-- refresh_token
-- -------------------------------------------------------------
CREATE TABLE refresh_token (
    id_refresh_token BIGSERIAL    PRIMARY KEY,
    token            VARCHAR(255) NOT NULL UNIQUE,
    id_usuario       BIGINT       NOT NULL REFERENCES usuario(id_usuario),
    fecha_expiracion TIMESTAMP    NOT NULL,
    revocado         BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_creacion   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- Índices
-- -------------------------------------------------------------
CREATE INDEX idx_cuenta_cliente            ON cuenta(id_cliente);
CREATE INDEX idx_movimiento_cuenta         ON movimiento(id_cuenta);
CREATE INDEX idx_transferencia_origen      ON transferencia(id_cuenta_origen);
CREATE INDEX idx_transferencia_destino     ON transferencia(id_cuenta_destino);
CREATE INDEX idx_transf_ext_origen         ON transferencia_externa(id_cuenta_origen);
CREATE INDEX idx_transf_int_origen         ON transferencia_internacional(id_cuenta_origen);
CREATE INDEX idx_auditoria_usuario         ON auditoria(id_usuario);
CREATE INDEX idx_refresh_token_usuario     ON refresh_token(id_usuario);

-- =============================================================
-- Datos semilla (equivalente al perfil "seed" de Spring Boot)
-- Contraseñas hasheadas con BCrypt strength 10:
--   bryan / bryan123     ana / ana123     carlos / carlos123
--   laura / laura123     jorge / jorge123     sofia / sofia123
-- =============================================================

INSERT INTO rol (nombre) VALUES ('ADMIN'), ('CLIENTE');

INSERT INTO cliente (nombre, documento, fecha_expedicion, email, direccion, telefono, genero) VALUES
    ('Bryan Molina',   '123456789', '2020-01-10', 'bryan@example.com',  'Calle 10 #20-30',       '3000000001', 'MASCULINO'),
    ('Ana Gómez',      '987654321', '2019-05-02', 'ana@example.com',    'Carrera 15 #8-45',      '3000000002', 'FEMENINO'),
    ('Carlos Pérez',   '111111111', '2021-07-19', 'carlos@example.com', 'Diagonal 50 #14-90',    '3000000003', 'MASCULINO'),
    ('Laura Martínez', '222222222', '2018-03-11', 'laura@example.com',  'Transversal 12 #45-67', '3000000004', 'FEMENINO'),
    ('Jorge Ramírez',  '333333333', '2017-09-30', 'jorge@example.com',  'Avenida 80 #30-12',     '3000000005', 'MASCULINO'),
    ('Sofía Vargas',   '444444444', '2022-06-15', 'sofia@example.com',  'Calle 5 #10-20',        '3000000006', 'FEMENINO');

INSERT INTO usuario (username, password_hash, estado, id_cliente) VALUES
    ('bryan',  '$2a$10$Pk8D8.TuVKew9nt8usnqKeHUrdpX.xsszkHKmT0WifJCwJ0Il9eHC', 'ACTIVO',    1),
    ('ana',    '$2a$10$QcSS89g4jkVBM1X3HCo15eLTXYOaxwuXAB.juNI7BETyqouPwgrXG', 'ACTIVO',    2),
    ('carlos', '$2a$10$0XT7WMtNLsIja/flwPhir.h8X1t8yUjYwqSlR2CVVp9dMjOpCplfq', 'ACTIVO',    3),
    ('laura',  '$2a$10$24Ea1uoKdu3koz6MqIQ63OF8/b0/RIdTaAU9DmwFz8EvqSo10u80y', 'ACTIVO',    4),
    ('jorge',  '$2a$10$n3mx4T/3tL2Feurfn7cZSuiYoSrK1UaU75puQMSy4wieF/dhQxSay', 'BLOQUEADO', 5),
    ('sofia',  '$2a$10$YTTAQ5CpgzXEIqIH9a2RceBBZ4Rg8TR3mN5wBrcAabaWOkfJOc/gK', 'ACTIVO',    6);

-- bryan = ADMIN, todos los demás = CLIENTE
INSERT INTO usuario_rol (id_usuario, id_rol) VALUES
    (1, 1), (2, 2), (3, 2), (4, 2), (5, 2), (6, 2);

INSERT INTO cuenta (numero_cuenta, tipo, estado, saldo, id_cliente, version) VALUES
    ('00010001', 'AHORROS',   'ACTIVA',   530000.0000,  1, 0),
    ('00020001', 'AHORROS',   'ACTIVA',   1225000.0000, 2, 0),
    ('00030001', 'AHORROS',   'ACTIVA',   0.0000,       3, 0),
    ('00040001', 'CORRIENTE', 'ACTIVA',   150000.0000,  4, 0),
    ('00050001', 'AHORROS',   'INACTIVA', 0.0000,       5, 0),
    ('00060001', 'AHORROS',   'ACTIVA',   500000.0000,  6, 0);
