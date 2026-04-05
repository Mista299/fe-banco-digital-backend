# 🗄️ Modelo de Base de Datos — Banco Digital

> Este documento describe el modelo lógico de la base de datos del sistema.  
> **Convención:** Todos los nombres de tablas, columnas, entidades JPA y variables de código están en **español**.

---

## Diagrama Entidad-Relación

```mermaid
erDiagram
  cliente {
    bigint id_cliente PK
    varchar nombre
    varchar documento UK
    varchar email UK
    varchar telefono
    timestamp fecha_registro
  }
  usuario {
    bigint id_usuario PK
    varchar username UK
    varchar password_hash
    varchar estado
    bigint id_cliente FK
  }
  rol {
    bigint id_rol PK
    varchar nombre UK
  }
  usuario_rol {
    bigint id_usuario FK
    bigint id_rol FK
  }
  cuenta {
    bigint id_cuenta PK
    varchar numero_cuenta UK
    varchar tipo
    decimal saldo
    varchar estado
    bigint id_cliente FK
  }
  transaccion {
    bigint id_transaccion PK
    bigint id_cuenta_origen FK
    bigint id_cuenta_destino FK
    varchar tipo
    decimal monto
    timestamp fecha
    varchar estado
  }
  auditoria {
    bigint id_auditoria PK
    varchar accion
    bigint id_usuario FK
    timestamp fecha
    text detalle
  }

  cliente ||--o{ cuenta : "tiene"
  cliente ||--|| usuario : "tiene"
  usuario ||--o{ usuario_rol : "pertenece a"
  rol ||--o{ usuario_rol : "asignado a"
  cuenta ||--o{ transaccion : "origen"
  cuenta ||--o{ transaccion : "destino"
  usuario ||--o{ auditoria : "genera"
```

---

## Relaciones del Modelo

| Relación | Cardinalidad | Descripción |
|---|---|---|
| `cliente` → `cuenta` | 1 a N | Un cliente puede tener múltiples cuentas |
| `cliente` → `usuario` | 1 a 1 | Cada cliente tiene exactamente un usuario de acceso |
| `usuario` → `usuario_rol` | 1 a N | Un usuario puede tener múltiples roles |
| `rol` → `usuario_rol` | 1 a N | Un rol puede estar asignado a múltiples usuarios |
| `cuenta` → `transaccion` (origen) | 1 a N | Una cuenta puede ser origen de múltiples transacciones |
| `cuenta` → `transaccion` (destino) | 1 a N | Una cuenta puede ser destino de múltiples transacciones |
| `usuario` → `auditoria` | 1 a N | Un usuario puede generar múltiples registros de auditoría |

> `usuario_rol` es una tabla intermedia que resuelve la relación N:M entre `usuario` y `rol`.  
> Su PK compuesta `(id_usuario, id_rol)` garantiza que no existan duplicados.

---

## Descripción de Tablas

### `cliente`
Almacena la información personal de los clientes del banco.

| Columna | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id_cliente` | `BIGINT` | PK, AUTO_INCREMENT | Identificador único del cliente |
| `nombre` | `VARCHAR(150)` | NOT NULL | Nombre completo del cliente |
| `documento` | `VARCHAR(20)` | NOT NULL, UNIQUE | Número de documento de identidad |
| `email` | `VARCHAR(100)` | NOT NULL, UNIQUE | Correo electrónico de contacto |
| `telefono` | `VARCHAR(20)` | NULLABLE | Teléfono de contacto |
| `fecha_registro` | `TIMESTAMP` | DEFAULT NOW() | Fecha y hora de registro en el sistema |

---

### `usuario`
Gestiona las credenciales de acceso al sistema. Cada usuario está ligado a exactamente un cliente.

| Columna | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id_usuario` | `BIGINT` | PK, AUTO_INCREMENT | Identificador único del usuario |
| `username` | `VARCHAR(50)` | NOT NULL, UNIQUE | Nombre de usuario para login |
| `password_hash` | `VARCHAR(255)` | NOT NULL | Contraseña encriptada con BCrypt |
| `estado` | `VARCHAR(20)` | NOT NULL | Estado del usuario: `ACTIVO`, `BLOQUEADO` |
| `id_cliente` | `BIGINT` | FK → `cliente`, NOT NULL, UNIQUE | Cliente asociado |

---

### `rol`
Catálogo de roles disponibles en el sistema (ej: `ADMIN`, `CLIENTE`, `CAJERO`).

| Columna | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id_rol` | `BIGINT` | PK, AUTO_INCREMENT | Identificador único del rol |
| `nombre` | `VARCHAR(50)` | NOT NULL, UNIQUE | Nombre del rol |

---

### `usuario_rol`
Tabla intermedia que representa la relación N:M entre `usuario` y `rol`.

| Columna | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id_usuario` | `BIGINT` | PK (compuesta), FK → `usuario` | Usuario asignado |
| `id_rol` | `BIGINT` | PK (compuesta), FK → `rol` | Rol asignado |

> La PK compuesta `(id_usuario, id_rol)` evita duplicidades en la asignación de roles.

---

### `cuenta`
Representa las cuentas bancarias de los clientes.

| Columna | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id_cuenta` | `BIGINT` | PK, AUTO_INCREMENT | Identificador único de la cuenta |
| `numero_cuenta` | `VARCHAR(20)` | NOT NULL, UNIQUE | Número de cuenta bancaria |
| `tipo` | `VARCHAR(20)` | NOT NULL | Tipo de cuenta: `AHORROS`, `CORRIENTE` |
| `saldo` | `DECIMAL(19,4)` | DEFAULT 0 | Saldo disponible en la cuenta |
| `estado` | `VARCHAR(20)` | NOT NULL | Estado: `ACTIVA`, `INACTIVA` |
| `id_cliente` | `BIGINT` | FK → `cliente`, NOT NULL | Propietario de la cuenta |

---

### `transaccion`
Registra todas las operaciones financieras realizadas en el sistema.

| Columna | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id_transaccion` | `BIGINT` | PK, AUTO_INCREMENT | Identificador único de la transacción |
| `id_cuenta_origen` | `BIGINT` | FK → `cuenta`, NULLABLE | Cuenta de origen (null en depósitos externos) |
| `id_cuenta_destino` | `BIGINT` | FK → `cuenta`, NULLABLE | Cuenta de destino (null en retiros) |
| `tipo` | `VARCHAR(20)` | NOT NULL | Tipo: `DEPOSITO`, `RETIRO`, `TRANSFERENCIA` |
| `monto` | `DECIMAL(19,4)` | NOT NULL | Monto de la operación |
| `fecha` | `TIMESTAMP` | DEFAULT NOW() | Fecha y hora de la transacción |
| `estado` | `VARCHAR(20)` | NOT NULL | Estado: `EXITOSA`, `FALLIDA` |

---

### `auditoria`
Registra las acciones relevantes realizadas por los usuarios en el sistema.

| Columna | Tipo | Restricciones | Descripción |
|---|---|---|---|
| `id_auditoria` | `BIGINT` | PK, AUTO_INCREMENT | Identificador único del registro |
| `accion` | `VARCHAR(100)` | NOT NULL | Descripción de la acción realizada |
| `id_usuario` | `BIGINT` | FK → `usuario`, NOT NULL | Usuario que realizó la acción |
| `fecha` | `TIMESTAMP` | DEFAULT NOW() | Fecha y hora de la acción |
| `detalle` | `TEXT` | NULLABLE | Información adicional o contexto |

---

## Mapeo Tabla ↔ Entidad JPA

| Tabla | Entidad JPA | Paquete |
|---|---|---|
| `cliente` | `Cliente` | `domain.entity` |
| `usuario` | `Usuario` | `domain.entity` |
| `rol` | `Rol` | `domain.entity` |
| `usuario_rol` | `UsuarioRol` | `domain.entity` |
| `cuenta` | `Cuenta` | `domain.entity` |
| `transaccion` | `Transaccion` | `domain.entity` |
| `auditoria` | `Auditoria` | `domain.entity` |

### Ejemplo — entidad `Cuenta`

```java
@Entity
@Table(name = "cuenta")
@Getter
@Setter
@NoArgsConstructor
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta")
    private Long idCuenta;

    @Column(name = "numero_cuenta", unique = true, nullable = false)
    private String numeroCuenta;

    @Column(name = "tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoCuenta tipo;

    @Column(name = "saldo", precision = 19, scale = 4)
    private BigDecimal saldo;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoCuenta estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;
}
```

### Ejemplo — entidad `Transaccion` (doble FK a `cuenta`)

```java
@Entity
@Table(name = "transaccion")
@Getter
@Setter
@NoArgsConstructor
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaccion")
    private Long idTransaccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta_origen")
    private Cuenta cuentaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta_destino")
    private Cuenta cuentaDestino;

    @Column(name = "tipo", nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoTransaccion tipo;

    @Column(name = "monto", nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoTransaccion estado;
}
```

### Ejemplo — entidad `UsuarioRol` (PK compuesta)

```java
@Entity
@Table(name = "usuario_rol")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioRol {

    @EmbeddedId
    private UsuarioRolId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario")
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idRol")
    @JoinColumn(name = "id_rol")
    private Rol rol;
}

@Embeddable
public class UsuarioRolId implements Serializable {

    @Column(name = "id_usuario")
    private Long idUsuario;

    @Column(name = "id_rol")
    private Long idRol;
}
```

---

## Enums del Dominio

```java
public enum TipoCuenta        { AHORROS, CORRIENTE }
public enum EstadoCuenta      { ACTIVA, INACTIVA }
public enum EstadoUsuario     { ACTIVO, BLOQUEADO }
public enum TipoTransaccion   { DEPOSITO, RETIRO, TRANSFERENCIA }
public enum EstadoTransaccion { EXITOSA, FALLIDA }
```

---

## Notas Importantes

- **Valores monetarios:** Usar siempre `DECIMAL(19,4)` en BD y `BigDecimal` en Java. Nunca `float` ni `double`.
- **Contraseñas:** El campo `password_hash` almacena el hash BCrypt. Nunca se guarda texto plano.
- **Auditoría:** Toda acción sensible (login, transferencia, cambio de estado) debe generar un registro en `auditoria`.
- **Soft delete:** Para eliminar lógicamente un registro usar el campo `estado`, no borrar físicamente.
- **Zona horaria:** Todos los campos `TIMESTAMP` se manejan en UTC a nivel de base de datos.
- **Fetch lazy:** Todas las relaciones `@ManyToOne` y `@OneToMany` usan `FetchType.LAZY` por defecto para evitar consultas innecesarias.

---

*Para dudas sobre el modelo, consultar con el líder técnico o el DBA del proyecto.*
