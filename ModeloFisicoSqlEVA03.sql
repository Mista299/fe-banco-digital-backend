-- ======================================
-- TABLA: cliente
-- ======================================
CREATE TABLE cliente (
    id_cliente INT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    documento VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    telefono VARCHAR(15),
    fecha_registro DATE
);

-- ======================================
-- TABLA: usuario
-- ======================================
CREATE TABLE usuario (
    id_usuario INT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    estado ENUM('ACTIVO','INACTIVO','BLOQUEADO') NOT NULL,
    id_cliente INT NOT NULL UNIQUE,
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
);

-- ======================================
-- TABLA: rol
-- ======================================
CREATE TABLE rol (
    id_rol INT PRIMARY KEY,
    nombre VARCHAR(20) NOT NULL UNIQUE
);

-- ======================================
-- TABLA: usuario_rol (relación muchos a muchos)
-- ======================================
CREATE TABLE usuario_rol (
    id_usuario INT,
    id_rol INT,
    PRIMARY KEY (id_usuario, id_rol),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);

-- ======================================
-- TABLA: cuenta
-- ======================================
CREATE TABLE cuenta (
    id_cuenta INT PRIMARY KEY,
    numero_cuenta VARCHAR(20) NOT NULL UNIQUE,
    tipo ENUM('AHORROS','CORRIENTE'),
    saldo DECIMAL(15,2),
    estado ENUM('ACTIVA','INACTIVA','BLOQUEADA'),
    id_cliente INT NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
);

-- ======================================
-- TABLA: transaccion
-- ======================================
CREATE TABLE transaccion (
    id_transaccion INT PRIMARY KEY,
    id_cuenta_origen INT,
    id_cuenta_destino INT,
    tipo_operacion ENUM('DEPOSITO','RETIRO','TRANSFERENCIA','TRANSFERENCIA_INTERBANCARIA','REVERSO_ACH') NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    fecha TIMESTAMP,
    estado ENUM('EXITOSA','FALLIDA','PENDIENTE_PROCESAMIENTO','RECHAZADA','REVERSADA'),
    banco_destino VARCHAR(120),
    tipo_cuenta_destino_externa VARCHAR(30),
    numero_cuenta_destino_externa VARCHAR(40),
    tipo_documento_receptor VARCHAR(30),
    numero_documento_receptor VARCHAR(40),
    nombre_receptor_externo VARCHAR(150),
    referencia_externa VARCHAR(80),
    motivo_rechazo VARCHAR(255),
    
    FOREIGN KEY (id_cuenta_origen) REFERENCES cuenta(id_cuenta),
    FOREIGN KEY (id_cuenta_destino) REFERENCES cuenta(id_cuenta)
);

-- ======================================
-- TABLA: auditoria
-- ======================================
CREATE TABLE auditoria (
    id_auditoria INT PRIMARY KEY,
    accion VARCHAR(100) NOT NULL,
    id_usuario INT NOT NULL,
    fecha TIMESTAMP,
    detalle TEXT,
    
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);