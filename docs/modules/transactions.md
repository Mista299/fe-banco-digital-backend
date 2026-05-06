# Módulo de Transacciones

## ¿Qué hace?

Registra y ejecuta los movimientos financieros del sistema: depósitos, retiros y transferencias.

## Responsabilidades

- Ejecutar transferencias internas (entre cuentas del mismo banco)
- Registrar depósitos y retiros
- Actualizar saldos de forma atómica (`@Transactional`)
- Registrar el estado de cada operación (EXITOSA / FALLIDA)

## Lo que NO hace

- No valida identidad del usuario — eso lo hace el módulo de autenticación
- No gestiona transferencias interbancarias (ACH) — previsto para Sprint 2

## Endpoints

| Método | Ruta | Descripción | Auth requerida |
|--------|------|-------------|----------------|
| `POST` | `/api/v1/transacciones/depositar` | Depositar dinero en una cuenta | Sí |
| `POST` | `/api/v1/transacciones/retirar` | Retirar dinero de una cuenta | Sí |
| `POST` | `/api/v1/transacciones/transferir` | Transferir entre cuentas | Sí |
| `GET` | `/api/v1/transacciones/cuenta/{idCuenta}` | Movimientos de una cuenta | Sí |
| `GET` | `/api/v1/transacciones/cuenta/{idCuenta}/filtro` | Movimientos filtrados por fechas | Sí |

> **Por qué se envía `idCuenta` en el body:** un cliente puede tener varias cuentas y debe elegir sobre cuál operar. El `idCuenta` no es un ID de identidad — es la selección del recurso. El servidor siempre verifica que la cuenta pertenezca al cliente del token; si no, devuelve `403 Forbidden`.

## Modelo de datos

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id_transaccion` | `BIGINT` PK | Identificador único |
| `id_cuenta_origen` | FK → `cuenta` NULLABLE | Nulo en depósitos externos |
| `id_cuenta_destino` | FK → `cuenta` NULLABLE | Nulo en retiros |
| `tipo` | `DEPOSITO` / `RETIRO` / `TRANSFERENCIA` | Tipo de operación |
| `monto` | `DECIMAL(19,4)` | Monto. Siempre `BigDecimal` en código. |
| `fecha` | `TIMESTAMP` | Fecha y hora en UTC |
| `estado` | `EXITOSA` / `FALLIDA` | Resultado de la operación |

## Reglas de negocio

- El saldo nunca puede quedar negativo — validar antes de debitar.
- Si una cuenta está `INACTIVA`, no puede operar.
- Los saldos de origen y destino se actualizan en la misma transacción de BD.
- Toda transacción genera un registro en `auditoria`.

## Dependencias

| Módulo / Clase | Para qué |
|----------------|----------|
| `CuentaRepository` | Leer y actualizar saldos; verificar propiedad de la cuenta |
| `TransaccionRepository` | Persistir el registro de la operación |
| `TransaccionMapper` | Convertir `Transaccion` → `TransaccionDTO` |
| `AuditoriaRepository` | Registrar la acción en el log de auditoría |
| `UsuarioRepository` | Obtener el cliente del usuario autenticado para validar propiedad |

## Iteration 2 - Nuevas capacidades

### Task 59 - Registro de transacción

El endpoint de depósito registra una fila en `transaccion` con fecha, monto, estado y `tipo_operacion = DEPOSITO`.

### Task 60 - Ajuste del modelo

La entidad `Transaccion` ahora mapea el tipo de movimiento en la columna `tipo_operacion`, permitiendo distinguir depósitos, retiros, transferencias internas, transferencias interbancarias y reversos ACH.

### HU-13 - Motor de Validación

Nuevo endpoint:

```http
POST /api/v1/validaciones/transaccion
```

Valida saldo y estado de cuenta antes de permitir operaciones de salida.

### HU-12 - Transferencias interbancarias

Nuevos endpoints:

```http
POST /api/v1/transferencias/interbancarias
POST /api/v1/transferencias/interbancarias/{idTransaccion}/rechazo-ach
```

El primer endpoint crea una orden ACH en estado `PENDIENTE_PROCESAMIENTO`; el segundo reversa fondos cuando la red ACH rechaza la operación.
