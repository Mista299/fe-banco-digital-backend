-- =============================================================
-- Banco Digital — Script SQL Sprint 2
-- Base de datos: banco2026 (PostgreSQL)
-- Última actualización: sincronizado con entidades JPA actuales
-- =============================================================

-- -------------------------------------------------------------
-- Limpieza (orden inverso a FK)
-- -------------------------------------------------------------
DROP TABLE IF EXISTS refresh_token   CASCADE;
DROP TABLE IF EXISTS token_retiro    CASCADE;
DROP TABLE IF EXISTS auditoria       CASCADE;
DROP TABLE IF EXISTS transaccion     CASCADE;
DROP TABLE IF EXISTS usuario_rol     CASCADE;
DROP TABLE IF EXISTS cuenta          CASCADE;
DROP TABLE IF EXISTS usuario         CASCADE;
DROP TABLE IF EXISTS rol             CASCADE;
DROP TABLE IF EXISTS cliente         CASCADE;

-- -------------------------------------------------------------
-- cliente
-- -------------------------------------------------------------
CREATE TABLE cliente (
    id_cliente       BIGSERIAL      PRIMARY KEY,
    nombre           VARCHAR(150)   NOT NULL,
    documento        VARCHAR(20)    NOT NULL UNIQUE,
    fecha_expedicion DATE           NOT NULL,
    email            VARCHAR(100)   NOT NULL UNIQUE,
    direccion        VARCHAR(255)   NOT NULL,
    telefono         VARCHAR(20),
    fecha_registro   TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- usuario
-- -------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario    BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    estado        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO'
                               CHECK (estado IN ('ACTIVO', 'INACTIVO', 'BLOQUEADO')),
    id_cliente    BIGINT       NOT NULL UNIQUE
                               REFERENCES cliente(id_cliente)
);

-- -------------------------------------------------------------
-- rol
-- -------------------------------------------------------------
CREATE TABLE rol (
    id_rol BIGSERIAL   PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

-- -------------------------------------------------------------
-- usuario_rol  (N:M entre usuario y rol)
-- -------------------------------------------------------------
CREATE TABLE usuario_rol (
    id_usuario BIGINT NOT NULL REFERENCES usuario(id_usuario),
    id_rol     BIGINT NOT NULL REFERENCES rol(id_rol),
    PRIMARY KEY (id_usuario, id_rol)
);

-- -------------------------------------------------------------
-- cuenta
-- Nota: columna `version` requerida por @Version de Hibernate
--       para control de concurrencia optimista.
-- -------------------------------------------------------------
CREATE TABLE cuenta (
    id_cuenta     BIGSERIAL      PRIMARY KEY,
    version       BIGINT         NOT NULL DEFAULT 0,
    numero_cuenta VARCHAR(20)    NOT NULL UNIQUE,
    tipo          VARCHAR(20)    NOT NULL
                                 CHECK (tipo IN ('AHORROS', 'CORRIENTE')),
    saldo         DECIMAL(19,4)  NOT NULL DEFAULT 0,
    estado        VARCHAR(20)    NOT NULL
                                 CHECK (estado IN ('ACTIVA', 'INACTIVA', 'BLOQUEADA')),
    id_cliente    BIGINT         NOT NULL REFERENCES cliente(id_cliente)
);

-- -------------------------------------------------------------
-- transaccion
-- -------------------------------------------------------------
CREATE TABLE transaccion (
    id_transaccion                BIGSERIAL      PRIMARY KEY,
    id_cuenta_origen              BIGINT         REFERENCES cuenta(id_cuenta),
    id_cuenta_destino             BIGINT         REFERENCES cuenta(id_cuenta),
    tipo                          VARCHAR(40)    NOT NULL
                                                 CHECK (tipo IN (
                                                     'DEPOSITO',
                                                     'RETIRO',
                                                     'TRANSFERENCIA',
                                                     'TRANSFERENCIA_INTERBANCARIA',
                                                     'REVERSO_ACH'
                                                 )),
    monto                         DECIMAL(19,4)  NOT NULL,
    fecha                         TIMESTAMP      NOT NULL DEFAULT NOW(),
    estado                        VARCHAR(30)    NOT NULL
                                                 CHECK (estado IN (
                                                     'EXITOSA',
                                                     'FALLIDA',
                                                     'PENDIENTE_PROCESAMIENTO',
                                                     'RECHAZADA',
                                                     'REVERSADA'
                                                 )),
    banco_destino                 VARCHAR(120),
    tipo_cuenta_destino_externa   VARCHAR(30),
    numero_cuenta_destino_externa VARCHAR(40),
    tipo_documento_receptor       VARCHAR(30),
    numero_documento_receptor     VARCHAR(40),
    nombre_receptor_externo       VARCHAR(150),
    referencia_externa            VARCHAR(80),
    motivo_rechazo                VARCHAR(255)
);

-- -------------------------------------------------------------
-- auditoria
-- -------------------------------------------------------------
CREATE TABLE auditoria (
    id_auditoria BIGSERIAL    PRIMARY KEY,
    accion       VARCHAR(100) NOT NULL,
    id_usuario   BIGINT       NOT NULL REFERENCES usuario(id_usuario),
    fecha        TIMESTAMP    NOT NULL DEFAULT NOW(),
    detalle      TEXT
);

-- -------------------------------------------------------------
-- refresh_token
-- -------------------------------------------------------------
CREATE TABLE refresh_token (
    id_refresh_token BIGSERIAL    PRIMARY KEY,
    token            VARCHAR(512) NOT NULL UNIQUE,
    id_usuario       BIGINT       NOT NULL REFERENCES usuario(id_usuario),
    fecha_expiracion TIMESTAMP    NOT NULL,
    revocado         BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_creacion   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
-- token_retiro  (códigos de 6 dígitos para retiro sin tarjeta)
-- -------------------------------------------------------------
CREATE TABLE token_retiro (
    id_token         BIGSERIAL      PRIMARY KEY,
    codigo           VARCHAR(6)     NOT NULL UNIQUE,
    estado           VARCHAR(20)    NOT NULL DEFAULT 'ACTIVO'
                                    CONSTRAINT token_retiro_estado_check
                                    CHECK (estado IN ('ACTIVO', 'USADO', 'EXPIRADO')),
    fecha_creacion   TIMESTAMP      NOT NULL DEFAULT NOW(),
    fecha_expiracion TIMESTAMP      NOT NULL,
    monto            DECIMAL(19,4)  NOT NULL,
    id_cuenta        BIGINT         NOT NULL REFERENCES cuenta(id_cuenta)
);

-- -------------------------------------------------------------
-- Índices
-- -------------------------------------------------------------
CREATE INDEX idx_cuenta_cliente        ON cuenta(id_cliente);
CREATE INDEX idx_transaccion_origen    ON transaccion(id_cuenta_origen);
CREATE INDEX idx_transaccion_destino   ON transaccion(id_cuenta_destino);
CREATE INDEX idx_auditoria_usuario     ON auditoria(id_usuario);
CREATE INDEX idx_refresh_token_usuario ON refresh_token(id_usuario);
CREATE INDEX idx_token_retiro_cuenta   ON token_retiro(id_cuenta);
CREATE INDEX idx_token_retiro_estado   ON token_retiro(estado);

-- =============================================================
-- Datos semilla (equivalente al perfil "seed" de Spring)
-- Las contraseñas están hasheadas con BCrypt.
--   bryan  → password: "password123"
--   ana    → password: "password123"
-- =============================================================

-- Roles
INSERT INTO rol (nombre) VALUES ('ADMIN'), ('CLIENTE');

-- Clientes
INSERT INTO cliente (nombre, documento, fecha_expedicion, email, direccion, telefono) VALUES
    ('Bryan García', '1000000001', '2000-01-15', 'bryan@mail.com', 'Calle 10 #20-30',  '3001000001'),
    ('Ana Martínez', '1000000002', '2001-06-20', 'ana@mail.com',   'Carrera 5 #15-40', '3001000002');

-- Usuarios (BCrypt de "password123")
INSERT INTO usuario (username, password_hash, estado, id_cliente) VALUES
    ('bryan', '$2a$10$7EqJtq98hPqEX7fNZaFWoOe3d8j1Fh5B5X2YmH3K9pQ0sL6vR4mGi', 'ACTIVO', 1),
    ('ana',   '$2a$10$7EqJtq98hPqEX7fNZaFWoOe3d8j1Fh5B5X2YmH3K9pQ0sL6vR4mGi', 'ACTIVO', 2);

-- Asignación de roles
INSERT INTO usuario_rol (id_usuario, id_rol) VALUES
    (1, 1),  -- bryan → ADMIN
    (1, 2),  -- bryan → CLIENTE
    (2, 2);  -- ana   → CLIENTE

-- Cuentas
INSERT INTO cuenta (numero_cuenta, tipo, saldo, estado, id_cliente) VALUES
    ('001-0000001', 'AHORROS',   5000000.0000, 'ACTIVA', 1),
    ('001-0000002', 'CORRIENTE', 2500000.0000, 'ACTIVA', 1),
    ('001-0000003', 'AHORROS',   1200000.0000, 'ACTIVA', 2);

-- Transacciones
INSERT INTO transaccion (id_cuenta_origen, id_cuenta_destino, tipo, monto, estado) VALUES
    (NULL, 1, 'DEPOSITO',      5000000.0000, 'EXITOSA'),
    (1,    3, 'TRANSFERENCIA',  300000.0000, 'EXITOSA'),
    (3, NULL, 'RETIRO',         100000.0000, 'EXITOSA');

-- Auditoría
INSERT INTO auditoria (accion, id_usuario, detalle) VALUES
    ('LOGIN',         1, 'Inicio de sesión exitoso desde 127.0.0.1'),
    ('TRANSFERENCIA', 1, 'Transferencia de $300.000 desde cuenta 001-0000001 a 001-0000003');
