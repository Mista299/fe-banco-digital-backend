-- =============================================================
--  Banco Digital — Script de ESQUEMA + BLINDAJE DE INTEGRIDAD
--  Base de datos : banco2026  (PostgreSQL 16+)
--  Sprint        : 3
--  Generado      : 2026-05-29
-- =============================================================
--
--  ESTE ARCHIVO contiene SOLO estructura y reglas (sin datos):
--    tablas, índices, constraints, funciones, triggers,
--    reconciliación, documentación y el usuario de aplicación.
--    Los datos a insertar viven en:  script-sprint3-datos.sql
--
--  ORDEN DE EJECUCIÓN:
--    1) psql -v clave_mista='LA_CLAVE' -d banco2026 -f script-sprint3.sql
--    2) psql -d banco2026 -f script-sprint3-datos.sql
--
--  La variable psql `clave_mista` es OBLIGATORIA (Sección 8): la
--  contraseña del usuario de aplicación NO se escribe en el archivo,
--  se pasa al ejecutar (no queda en git).
--
--  CARACTERÍSTICAS:
--    * NO destructivo: no contiene DROP TABLE. Nada se borra.
--    * Idempotente: se puede ejecutar varias veces sin romper nada
--      (CREATE ... IF NOT EXISTS / OR REPLACE, DROP CONSTRAINT/TRIGGER
--      IF EXISTS + ADD/CREATE, rol creado solo si no existe).
--    * Compatible con Hibernate spring.jpa.hibernate.ddl-auto=validate
--      (los CHECK, triggers, la tabla libro_mayor y las vistas son
--      invisibles para la validación de Hibernate).
--
--  MODELO DE INTEGRIDAD (Opción C):
--    La aplicación sigue moviendo el saldo igual que hoy (sin cambios
--    en el código Java). La base de datos actúa como red de seguridad:
--      1. CHECK constraints estrictos: rechazan datos inválidos/vacíos.
--      2. Saldo nunca negativo (CHECK + trigger con mensaje claro).
--      3. Histórico contable inmutable (movimiento, transferencia,
--         libro_mayor y auditoria son append-only).
--      4. Libro mayor espejo: cada cambio de saldo se registra solo,
--         por trigger, con saldo antes/después (trazabilidad total).
--      5. Reconciliación: vista + función para detectar descuadres.
--
--  NOTA sobre transferencia_internacional:
--    El feature no está terminado en este sprint, por lo que NO se le
--    aplican constraints ni triggers dedicados. (El trigger genérico
--    de libro mayor sobre `cuenta` sí registrará cualquier cambio de
--    saldo que produzca, lo cual es deseable y no toca el feature.)
-- =============================================================


-- #############################################################
-- ##  SECCIÓN 1 — TABLAS                                       ##
-- ##  CREATE ... IF NOT EXISTS (no borra nada).               ##
-- ##  Orden: sin dependencias → con dependencias.            ##
-- #############################################################

-- -------------------------------------------------------------
--  1.1  cliente
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cliente (
    id_cliente       BIGSERIAL    PRIMARY KEY,
    nombre           VARCHAR(255) NOT NULL,
    documento        VARCHAR(255) NOT NULL UNIQUE,
    fecha_expedicion DATE         NOT NULL,
    email            VARCHAR(255) NOT NULL UNIQUE,
    direccion        VARCHAR(255) NOT NULL,
    telefono         VARCHAR(255),
    genero           VARCHAR(20)  NOT NULL CHECK (genero IN ('MASCULINO', 'FEMENINO')),
    fecha_registro   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
--  1.2  rol
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rol (
    id_rol BIGSERIAL   PRIMARY KEY,
    nombre VARCHAR(20) NOT NULL UNIQUE CHECK (nombre IN ('ADMIN', 'CLIENTE', 'GERENTE'))
);

-- -------------------------------------------------------------
--  1.3  usuario
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario (
    id_usuario    BIGSERIAL    PRIMARY KEY,
    username      VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    estado        VARCHAR(20)  NOT NULL CHECK (estado IN ('ACTIVO', 'INACTIVO', 'BLOQUEADO')),
    id_cliente    BIGINT       NOT NULL UNIQUE REFERENCES cliente(id_cliente)
);

-- -------------------------------------------------------------
--  1.4  usuario_rol
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuario_rol (
    id_usuario BIGINT NOT NULL REFERENCES usuario(id_usuario),
    id_rol     BIGINT NOT NULL REFERENCES rol(id_rol),
    PRIMARY KEY (id_usuario, id_rol)
);

-- -------------------------------------------------------------
--  1.5  cuenta
--  La columna `version` es requerida por @Version de Hibernate
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS cuenta (
    id_cuenta     BIGSERIAL     PRIMARY KEY,
    version       BIGINT        NOT NULL DEFAULT 0,
    numero_cuenta VARCHAR(255)  NOT NULL UNIQUE,
    tipo          VARCHAR(20)   NOT NULL CHECK (tipo IN ('AHORROS', 'CORRIENTE')),
    saldo         DECIMAL(19,4) NOT NULL DEFAULT 0,
    estado        VARCHAR(30)   NOT NULL CHECK (estado IN ('ACTIVA', 'INACTIVA', 'BLOQUEADA', 'PENDIENTE_APROBACION')),
    id_cliente    BIGINT        NOT NULL REFERENCES cliente(id_cliente)
);

-- -------------------------------------------------------------
--  1.6  movimiento  (depósitos y retiros via pasarela/cajero)
--  Append-only (ver Sección 5).
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS movimiento (
    id_movimiento BIGSERIAL     PRIMARY KEY,
    id_cuenta     BIGINT        NOT NULL REFERENCES cuenta(id_cuenta),
    tipo          VARCHAR(20)   NOT NULL CHECK (tipo IN ('DEPOSITO', 'RETIRO')),
    monto         DECIMAL(19,4) NOT NULL,
    fecha         TIMESTAMP     NOT NULL DEFAULT NOW(),
    estado        VARCHAR(20)   NOT NULL CHECK (estado IN ('EXITOSO', 'FALLIDO')),
    descripcion   VARCHAR(255)
);

-- -------------------------------------------------------------
--  1.7  transferencia  (interna entre cuentas del banco)
--  Append-only (ver Sección 5).
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transferencia (
    id_transferencia  BIGSERIAL     PRIMARY KEY,
    id_cuenta_origen  BIGINT        NOT NULL REFERENCES cuenta(id_cuenta),
    id_cuenta_destino BIGINT        NOT NULL REFERENCES cuenta(id_cuenta),
    monto             DECIMAL(19,4) NOT NULL,
    fecha             TIMESTAMP     NOT NULL DEFAULT NOW(),
    estado            VARCHAR(20)   NOT NULL CHECK (estado IN ('EXITOSA', 'FALLIDA'))
);

-- -------------------------------------------------------------
--  1.8  transferencia_externa  (ACH interbancaria — máquina de estado)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transferencia_externa (
    id_transf_ext              BIGSERIAL     PRIMARY KEY,
    id_cuenta_origen           BIGINT        NOT NULL REFERENCES cuenta(id_cuenta),
    banco_destino              VARCHAR(120)  NOT NULL,
    tipo_cuenta_destino        VARCHAR(30)   NOT NULL,
    numero_cuenta_destino      VARCHAR(40)   NOT NULL,
    tipo_documento_receptor    VARCHAR(30)   NOT NULL,
    numero_documento_receptor  VARCHAR(40)   NOT NULL,
    nombre_receptor            VARCHAR(150)  NOT NULL,
    monto                      DECIMAL(19,4) NOT NULL,
    fecha                      TIMESTAMP     NOT NULL DEFAULT NOW(),
    estado                     VARCHAR(30)   NOT NULL CHECK (estado IN ('PENDIENTE_PROCESAMIENTO', 'EXITOSA', 'RECHAZADA', 'REVERSADA')),
    referencia_externa         VARCHAR(80),
    motivo_rechazo             VARCHAR(255),
    id_transf_original         BIGINT        REFERENCES transferencia_externa(id_transf_ext)
);

-- -------------------------------------------------------------
--  1.9  transferencia_internacional  (SWIFT)
--  Feature no terminado en este sprint: SIN blindaje dedicado.
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS transferencia_internacional (
    id_transf_int              BIGSERIAL     PRIMARY KEY,
    id_cuenta_origen           BIGINT        NOT NULL REFERENCES cuenta(id_cuenta),
    banco_destino              VARCHAR(150)  NOT NULL,
    codigo_swift               VARCHAR(11)   NOT NULL,
    pais_destino               VARCHAR(60)   NOT NULL,
    tipo_cuenta_destino        VARCHAR(30)   NOT NULL,
    iban_cuenta_destino        VARCHAR(34)   NOT NULL,
    tipo_documento_receptor    VARCHAR(30)   NOT NULL,
    numero_documento_receptor  VARCHAR(40)   NOT NULL,
    nombre_receptor            VARCHAR(150)  NOT NULL,
    monto_usd                  DECIMAL(19,4) NOT NULL,
    tasa_cambio                DECIMAL(19,6) NOT NULL,
    monto_cop                  DECIMAL(19,4) NOT NULL,
    moneda                     VARCHAR(3)    NOT NULL DEFAULT 'USD',
    fecha                      TIMESTAMP     NOT NULL DEFAULT NOW(),
    estado                     VARCHAR(30)   NOT NULL CHECK (estado IN ('PENDIENTE_PROCESAMIENTO', 'EXITOSA', 'RECHAZADA', 'REVERSADA')),
    referencia_swift           VARCHAR(80),
    motivo_rechazo             VARCHAR(255)
);

-- -------------------------------------------------------------
--  1.10  deposito_pendiente  (código de pago para punto físico/PSE)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS deposito_pendiente (
    id                  BIGSERIAL     PRIMARY KEY,
    referencia_gateway  VARCHAR(255)  NOT NULL UNIQUE,
    numero_cuenta       VARCHAR(255)  NOT NULL,
    monto               DECIMAL(19,2) NOT NULL,
    estado              VARCHAR(20)   NOT NULL CHECK (estado IN ('PENDIENTE', 'COMPLETADO', 'EXPIRADO')),
    fecha_creacion      TIMESTAMP     NOT NULL DEFAULT NOW(),
    fecha_expiracion    TIMESTAMP     NOT NULL
);

-- -------------------------------------------------------------
--  1.11  token_retiro  (código de 6 dígitos para retiro en cajero)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS token_retiro (
    id_token         BIGSERIAL     PRIMARY KEY,
    codigo           VARCHAR(6)    NOT NULL UNIQUE,
    monto            DECIMAL(19,4) NOT NULL,
    fecha_expiracion TIMESTAMP     NOT NULL,
    estado           VARCHAR(20)   NOT NULL CHECK (estado IN ('ACTIVO', 'USADO', 'EXPIRADO')),
    id_cuenta        BIGINT        NOT NULL REFERENCES cuenta(id_cuenta),
    fecha_creacion   TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
--  1.12  auditoria  (append-only, ver Sección 5)
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS auditoria (
    id_auditoria BIGSERIAL    PRIMARY KEY,
    accion       VARCHAR(255) NOT NULL,
    id_usuario   BIGINT       NOT NULL REFERENCES usuario(id_usuario),
    fecha        TIMESTAMP    NOT NULL DEFAULT NOW(),
    detalle      TEXT
);

-- -------------------------------------------------------------
--  1.13  refresh_token
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_token (
    id_refresh_token BIGSERIAL    PRIMARY KEY,
    token            VARCHAR(255) NOT NULL UNIQUE,
    id_usuario       BIGINT       NOT NULL REFERENCES usuario(id_usuario),
    fecha_expiracion TIMESTAMP    NOT NULL,
    revocado         BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_creacion   TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- -------------------------------------------------------------
--  1.14  libro_mayor  (espejo contable append-only)
--  Tabla SIN entidad JPA. Cada cambio de saldo de una cuenta deja
--  aquí un asiento con saldo antes/después. Es la fuente de verdad
--  para auditoría y reconciliación. La llenan los triggers (Sección 5).
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS libro_mayor (
    id_asiento       BIGSERIAL     PRIMARY KEY,
    id_cuenta        BIGINT        NOT NULL REFERENCES cuenta(id_cuenta),
    saldo_anterior   DECIMAL(19,4) NOT NULL,
    monto            DECIMAL(19,4) NOT NULL CHECK (monto >= 0),
    tipo_asiento     VARCHAR(20)   NOT NULL CHECK (tipo_asiento IN ('APERTURA', 'CREDITO', 'DEBITO')),
    saldo_resultante DECIMAL(19,4) NOT NULL CHECK (saldo_resultante >= 0),
    fecha            TIMESTAMP     NOT NULL DEFAULT NOW()
);


-- #############################################################
-- ##  SECCIÓN 2 — ÍNDICES                                      ##
-- #############################################################
CREATE INDEX IF NOT EXISTS idx_cuenta_cliente           ON cuenta(id_cliente);
CREATE INDEX IF NOT EXISTS idx_movimiento_cuenta        ON movimiento(id_cuenta);
CREATE INDEX IF NOT EXISTS idx_transferencia_origen     ON transferencia(id_cuenta_origen);
CREATE INDEX IF NOT EXISTS idx_transferencia_destino    ON transferencia(id_cuenta_destino);
CREATE INDEX IF NOT EXISTS idx_transf_ext_origen        ON transferencia_externa(id_cuenta_origen);
CREATE INDEX IF NOT EXISTS idx_transf_int_origen        ON transferencia_internacional(id_cuenta_origen);
CREATE INDEX IF NOT EXISTS idx_deposito_referencia      ON deposito_pendiente(referencia_gateway);
CREATE INDEX IF NOT EXISTS idx_deposito_numero_cuenta   ON deposito_pendiente(numero_cuenta);
CREATE INDEX IF NOT EXISTS idx_token_retiro_cuenta      ON token_retiro(id_cuenta);
CREATE INDEX IF NOT EXISTS idx_auditoria_usuario        ON auditoria(id_usuario);
CREATE INDEX IF NOT EXISTS idx_refresh_token_usuario    ON refresh_token(id_usuario);
CREATE INDEX IF NOT EXISTS idx_libro_mayor_cuenta       ON libro_mayor(id_cuenta);
CREATE INDEX IF NOT EXISTS idx_libro_mayor_cuenta_fecha ON libro_mayor(id_cuenta, id_asiento DESC);


-- #############################################################
-- ##  SECCIÓN 3 — CONSTRAINTS ESTRICTOS                        ##
-- ##  Idempotentes: DROP CONSTRAINT IF EXISTS (no borra datos) ##
-- ##  + ADD. Si filas existentes violan una regla, el ADD     ##
-- ##  falla: limpiar esos datos antes de re-ejecutar.        ##
-- #############################################################

-- -------------------------------------------------------------
--  3.1  cliente — formatos y campos no vacíos
-- -------------------------------------------------------------
ALTER TABLE cliente DROP CONSTRAINT IF EXISTS chk_cliente_documento_formato;
ALTER TABLE cliente ADD  CONSTRAINT chk_cliente_documento_formato
    CHECK (documento ~ '^[0-9]{5,20}$');

ALTER TABLE cliente DROP CONSTRAINT IF EXISTS chk_cliente_email_formato;
ALTER TABLE cliente ADD  CONSTRAINT chk_cliente_email_formato
    CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$');

ALTER TABLE cliente DROP CONSTRAINT IF EXISTS chk_cliente_nombre_no_vacio;
ALTER TABLE cliente ADD  CONSTRAINT chk_cliente_nombre_no_vacio
    CHECK (length(btrim(nombre)) > 0);

ALTER TABLE cliente DROP CONSTRAINT IF EXISTS chk_cliente_direccion_no_vacia;
ALTER TABLE cliente ADD  CONSTRAINT chk_cliente_direccion_no_vacia
    CHECK (length(btrim(direccion)) > 0);

ALTER TABLE cliente DROP CONSTRAINT IF EXISTS chk_cliente_telefono_formato;
ALTER TABLE cliente ADD  CONSTRAINT chk_cliente_telefono_formato
    CHECK (telefono IS NULL OR telefono ~ '^[+0-9 ()-]{7,20}$');

-- -------------------------------------------------------------
--  3.2  usuario — campos no vacíos
-- -------------------------------------------------------------
ALTER TABLE usuario DROP CONSTRAINT IF EXISTS chk_usuario_username_no_vacio;
ALTER TABLE usuario ADD  CONSTRAINT chk_usuario_username_no_vacio
    CHECK (length(btrim(username)) > 0);

ALTER TABLE usuario DROP CONSTRAINT IF EXISTS chk_usuario_password_no_vacio;
ALTER TABLE usuario ADD  CONSTRAINT chk_usuario_password_no_vacio
    CHECK (length(btrim(password_hash)) > 0);

-- -------------------------------------------------------------
--  3.3  cuenta — saldo no negativo + formato número de cuenta
-- -------------------------------------------------------------
ALTER TABLE cuenta DROP CONSTRAINT IF EXISTS chk_cuenta_saldo_no_negativo;
ALTER TABLE cuenta ADD  CONSTRAINT chk_cuenta_saldo_no_negativo
    CHECK (saldo >= 0);

ALTER TABLE cuenta DROP CONSTRAINT IF EXISTS chk_cuenta_numero_formato;
ALTER TABLE cuenta ADD  CONSTRAINT chk_cuenta_numero_formato
    CHECK (numero_cuenta ~ '^[0-9]{8,20}$');

-- -------------------------------------------------------------
--  3.4  movimiento — monto positivo
-- -------------------------------------------------------------
ALTER TABLE movimiento DROP CONSTRAINT IF EXISTS chk_movimiento_monto_positivo;
ALTER TABLE movimiento ADD  CONSTRAINT chk_movimiento_monto_positivo
    CHECK (monto > 0);

-- -------------------------------------------------------------
--  3.5  transferencia — monto positivo + cuentas distintas
-- -------------------------------------------------------------
ALTER TABLE transferencia DROP CONSTRAINT IF EXISTS chk_transferencia_monto_positivo;
ALTER TABLE transferencia ADD  CONSTRAINT chk_transferencia_monto_positivo
    CHECK (monto > 0);

ALTER TABLE transferencia DROP CONSTRAINT IF EXISTS chk_transferencia_cuentas_distintas;
ALTER TABLE transferencia ADD  CONSTRAINT chk_transferencia_cuentas_distintas
    CHECK (id_cuenta_origen <> id_cuenta_destino);

-- -------------------------------------------------------------
--  3.6  transferencia_externa — monto positivo + formato destino
-- -------------------------------------------------------------
ALTER TABLE transferencia_externa DROP CONSTRAINT IF EXISTS chk_transf_ext_monto_positivo;
ALTER TABLE transferencia_externa ADD  CONSTRAINT chk_transf_ext_monto_positivo
    CHECK (monto > 0);

ALTER TABLE transferencia_externa DROP CONSTRAINT IF EXISTS chk_transf_ext_cuenta_destino_formato;
ALTER TABLE transferencia_externa ADD  CONSTRAINT chk_transf_ext_cuenta_destino_formato
    CHECK (numero_cuenta_destino ~ '^[0-9]{6,34}$');

-- NOTA sobre id_transf_original (autorreferencia para reverso/storno ACH):
-- La columna existe en el modelo y queda PREPARADA para el patrón storno, pero
-- ese flujo NO se alcanzó a implementar en este sprint: hoy el reverso se hace
-- mutando el estado a 'REVERSADA' y la columna permanece siempre NULL. Por eso
-- aquí NO se añaden CHECK ni índice único: serían infraestructura inerte sobre
-- una funcionalidad inexistente (YAGNI). Su intención se documenta en la
-- Sección 7 con COMMENT ON.

-- -------------------------------------------------------------
--  3.7  token_retiro — monto positivo + código de 6 dígitos
-- -------------------------------------------------------------
ALTER TABLE token_retiro DROP CONSTRAINT IF EXISTS chk_token_monto_positivo;
ALTER TABLE token_retiro ADD  CONSTRAINT chk_token_monto_positivo
    CHECK (monto > 0);

ALTER TABLE token_retiro DROP CONSTRAINT IF EXISTS chk_token_codigo_formato;
ALTER TABLE token_retiro ADD  CONSTRAINT chk_token_codigo_formato
    CHECK (codigo ~ '^[0-9]{6}$');

-- -------------------------------------------------------------
--  3.8  deposito_pendiente — monto positivo + coherencia de fechas
--       + FK lógica a cuenta(numero_cuenta)
-- -------------------------------------------------------------
ALTER TABLE deposito_pendiente DROP CONSTRAINT IF EXISTS chk_deposito_monto_positivo;
ALTER TABLE deposito_pendiente ADD  CONSTRAINT chk_deposito_monto_positivo
    CHECK (monto > 0);

ALTER TABLE deposito_pendiente DROP CONSTRAINT IF EXISTS chk_deposito_fechas_coherentes;
ALTER TABLE deposito_pendiente ADD  CONSTRAINT chk_deposito_fechas_coherentes
    CHECK (fecha_expiracion > fecha_creacion);

-- FK lógica: la entidad DepositoPendiente enlaza la cuenta por numero_cuenta.
-- cuenta.numero_cuenta es UNIQUE (destino válido de FK) y Hibernate
-- ddl-auto=validate no inspecciona FKs, así que no afecta el arranque.
ALTER TABLE deposito_pendiente DROP CONSTRAINT IF EXISTS fk_deposito_numero_cuenta;
ALTER TABLE deposito_pendiente ADD  CONSTRAINT fk_deposito_numero_cuenta
    FOREIGN KEY (numero_cuenta) REFERENCES cuenta(numero_cuenta);


-- #############################################################
-- ##  SECCIÓN 4 — FUNCIONES                                    ##
-- ##  (Lógica que invocan los triggers de la Sección 5.)      ##
-- #############################################################

-- -------------------------------------------------------------
--  4.1  fn_bloquear_modificacion — inmutabilidad (append-only)
--  Bloquea UPDATE/DELETE sobre tablas de histórico contable.
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_bloquear_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION
        'La tabla "%" es de solo lectura (append-only): no se permite % de registros existentes.',
        TG_TABLE_NAME, TG_OP
        USING ERRCODE = 'restrict_violation';
END;
$$ LANGUAGE plpgsql;

-- -------------------------------------------------------------
--  4.2  fn_validar_saldo_no_negativo — mensaje claro en español
--  Respaldo del CHECK chk_cuenta_saldo_no_negativo.
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_validar_saldo_no_negativo()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.saldo < 0 THEN
        RAISE EXCEPTION
            'Operación rechazada: el saldo de la cuenta % no puede quedar negativo (resultado: %).',
            NEW.numero_cuenta, NEW.saldo
            USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- -------------------------------------------------------------
--  4.3  fn_libro_mayor_registrar — espejo contable
--  Registra cada cambio de saldo de una cuenta como un asiento,
--  con saldo antes/después. Se dispara SOLO en UPDATE (movimientos
--  reales de dinero). Los asientos de apertura de las cuentas que
--  nacen con saldo > 0 se generan en script-sprint3-datos.sql.
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_libro_mayor_registrar()
RETURNS TRIGGER AS $$
DECLARE
    v_delta NUMERIC(19,4);
BEGIN
    -- Solo registramos si el saldo realmente cambió
    IF NEW.saldo IS DISTINCT FROM OLD.saldo THEN
        v_delta := NEW.saldo - OLD.saldo;
        INSERT INTO libro_mayor (id_cuenta, saldo_anterior, monto, tipo_asiento, saldo_resultante)
        VALUES (
            NEW.id_cuenta,
            OLD.saldo,
            abs(v_delta),
            CASE WHEN v_delta > 0 THEN 'CREDITO' ELSE 'DEBITO' END,
            NEW.saldo
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- #############################################################
-- ##  SECCIÓN 5 — TRIGGERS  (TODOS JUNTOS)                     ##
-- ##  Idempotentes: DROP TRIGGER IF EXISTS + CREATE.          ##
-- #############################################################

-- -------------------------------------------------------------
--  5.1  Inmutabilidad del histórico contable (append-only)
--  Solo tablas que la aplicación NUNCA actualiza ni borra.
-- -------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_movimiento_inmutable ON movimiento;
CREATE TRIGGER trg_movimiento_inmutable
    BEFORE UPDATE OR DELETE ON movimiento
    FOR EACH ROW EXECUTE FUNCTION fn_bloquear_modificacion();

DROP TRIGGER IF EXISTS trg_transferencia_inmutable ON transferencia;
CREATE TRIGGER trg_transferencia_inmutable
    BEFORE UPDATE OR DELETE ON transferencia
    FOR EACH ROW EXECUTE FUNCTION fn_bloquear_modificacion();

DROP TRIGGER IF EXISTS trg_auditoria_inmutable ON auditoria;
CREATE TRIGGER trg_auditoria_inmutable
    BEFORE UPDATE OR DELETE ON auditoria
    FOR EACH ROW EXECUTE FUNCTION fn_bloquear_modificacion();

DROP TRIGGER IF EXISTS trg_libro_mayor_inmutable ON libro_mayor;
CREATE TRIGGER trg_libro_mayor_inmutable
    BEFORE UPDATE OR DELETE ON libro_mayor
    FOR EACH ROW EXECUTE FUNCTION fn_bloquear_modificacion();

-- -------------------------------------------------------------
--  5.2  Saldo no negativo (antes de insertar/actualizar cuenta)
-- -------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_cuenta_saldo_no_negativo ON cuenta;
CREATE TRIGGER trg_cuenta_saldo_no_negativo
    BEFORE INSERT OR UPDATE ON cuenta
    FOR EACH ROW EXECUTE FUNCTION fn_validar_saldo_no_negativo();

-- -------------------------------------------------------------
--  5.3  Libro mayor espejo (después de cambiar el saldo de cuenta)
--  Solo AFTER UPDATE: cada movimiento real de dinero queda asentado.
-- -------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_cuenta_libro_mayor ON cuenta;
CREATE TRIGGER trg_cuenta_libro_mayor
    AFTER UPDATE ON cuenta
    FOR EACH ROW EXECUTE FUNCTION fn_libro_mayor_registrar();


-- #############################################################
-- ##  SECCIÓN 6 — RECONCILIACIÓN                              ##
-- ##  Detecta cuentas cuyo saldo no coincide con el último    ##
-- ##  asiento del libro mayor (descuadre / manipulación).     ##
-- #############################################################
CREATE OR REPLACE VIEW v_reconciliacion_saldos AS
SELECT
    c.id_cuenta,
    c.numero_cuenta,
    c.saldo                                  AS saldo_actual,
    lm.saldo_resultante                      AS saldo_libro_mayor,
    CASE
        WHEN lm.saldo_resultante IS NULL THEN
            CASE WHEN c.saldo = 0 THEN 'OK' ELSE 'SIN_ASIENTOS' END
        WHEN c.saldo = lm.saldo_resultante THEN 'OK'
        ELSE 'DESCUADRE'
    END                                      AS estado_conciliacion
FROM cuenta c
LEFT JOIN LATERAL (
    SELECT saldo_resultante
    FROM libro_mayor
    WHERE id_cuenta = c.id_cuenta
    ORDER BY id_asiento DESC
    LIMIT 1
) lm ON TRUE;

-- Devuelve únicamente las cuentas descuadradas (para alertas/monitoreo).
CREATE OR REPLACE FUNCTION fn_verificar_integridad()
RETURNS TABLE (
    id_cuenta         BIGINT,
    numero_cuenta     VARCHAR,
    saldo_actual      DECIMAL(19,4),
    saldo_libro_mayor DECIMAL(19,4)
) AS $$
    SELECT id_cuenta, numero_cuenta, saldo_actual, saldo_libro_mayor
    FROM v_reconciliacion_saldos
    WHERE estado_conciliacion = 'DESCUADRE';
$$ LANGUAGE sql STABLE;


-- #############################################################
-- ##  SECCIÓN 7 — DOCUMENTACIÓN DE SEMÁNTICA (COMMENT ON)     ##
-- ##  Los comentarios viven en el catálogo de PostgreSQL      ##
-- ##  (visibles con \d+ o consultando pg_description).        ##
-- #############################################################

COMMENT ON COLUMN transferencia_externa.id_transf_original IS
$doc$Autorreferencia prevista para asientos de contrapartida (patrón storno / reverso ACH).
Semántica de diseño (objetivo a futuro):
- NULL en toda transferencia PRIMARIA (la que origina el cliente).
- No nula SOLO en una fila de tipo reverso, apuntando a la transferencia primaria
  que anula/devuelve. Una fila con este campo no nulo sería un asiento correctivo
  y no debería reversarse de nuevo.

ESTADO ACTUAL (Sprint 3): NO IMPLEMENTADO. El flujo Java reversa mutando la fila
a estado 'REVERSADA' y esta columna permanece siempre NULL. Quedó definida en el
modelo como preparación, pero el patrón storno no se alcanzó a desarrollar en
este sprint; por eso no tiene CHECK ni índice asociados todavía. Implementar
junto con el refactor de TransferenciaInterbancariaServiceImpl si se retoma.$doc$;

COMMENT ON TABLE  libro_mayor IS
'Libro mayor espejo (append-only). Un asiento por cada cambio de saldo de una cuenta, con saldo antes/después. Lo llenan los triggers; es la fuente de verdad para auditoría y reconciliación.';

COMMENT ON COLUMN deposito_pendiente.numero_cuenta IS
'FK a cuenta(numero_cuenta) (UNIQUE). La cuenta destino del depósito se enlaza por número, no por id, porque así la identifica la pasarela de pagos.';


-- #############################################################
-- ##  SECCIÓN 8 — USUARIO DE APLICACIÓN `mista` Y PERMISOS    ##
-- #############################################################
--
--  Usuario de aplicación dedicado con permisos ACOTADOS (no superusuario):
--  solo lo que la app necesita para operar sobre el esquema `public`.
--  Estándar a futuro para producción (hoy el despliegue usa `postgres`).
--
--  La contraseña NO se escribe aquí: se pasa por la variable psql
--  `clave_mista` al ejecutar. Si la variable falta, el script aborta.
--      psql -v clave_mista='LA_CLAVE' -d banco2026 -f script-sprint3.sql
--
--  NOTA: el GRANT CONNECT usa el nombre de base `banco2026`. Si tu base
--  tiene otro nombre, ajústalo en la línea correspondiente.
-- #############################################################

-- 8.0  Guarda: si no se pasó -v clave_mista, abortar con mensaje claro.
--      (Sin esto, el error sería un críptico "syntax error near :".)
\if :{?clave_mista}
\else
\echo '*** ERROR: falta la variable psql clave_mista.'
\echo '*** Ejecuta:  psql -v clave_mista=''LA_CLAVE'' -d banco2026 -f script-sprint3.sql'
\quit
\endif

-- 8.1  Crear el rol solo si no existe (idempotente). \gexec ejecuta el
--      CREATE generado; %L cita la contraseña recibida por -v clave_mista.
SELECT format('CREATE ROLE mista LOGIN PASSWORD %L', :'clave_mista')
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'mista')
\gexec

-- 8.2  Si el rol ya existía, sincroniza la contraseña con la variable.
SELECT format('ALTER ROLE mista LOGIN PASSWORD %L', :'clave_mista')
WHERE EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'mista')
\gexec

-- 8.3  Permisos sobre la base y el esquema
GRANT CONNECT ON DATABASE banco2026 TO mista;
GRANT USAGE   ON SCHEMA   public    TO mista;

-- 8.4  Permisos sobre los objetos EXISTENTES
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES    IN SCHEMA public TO mista;
GRANT USAGE, SELECT                  ON ALL SEQUENCES IN SCHEMA public TO mista;
GRANT EXECUTE                        ON ALL FUNCTIONS IN SCHEMA public TO mista;

-- 8.5  Permisos por defecto para objetos FUTUROS (tablas/secuencias/funciones
--      que se creen después de este script heredan estos privilegios).
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO mista;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO mista;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT EXECUTE ON FUNCTIONS TO mista;


-- =============================================================
--  FIN DEL SCRIPT DE ESQUEMA
--
--  Siguiente paso — cargar datos:
--    psql -d banco2026 -f script-sprint3-datos.sql
--
--  Verificación rápida tras cargar datos:
--    SELECT * FROM v_reconciliacion_saldos;
--    SELECT * FROM fn_verificar_integridad();   -- vacío = todo OK
-- =============================================================
