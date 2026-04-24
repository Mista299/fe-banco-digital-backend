# Módulo de Cuentas

## ¿Qué hace?

Representa las cuentas bancarias de los clientes. Gestiona su estado, tipo y saldo.

## Responsabilidades

- Asociar cuentas a clientes (relación N:1)
- Mantener el saldo actualizado tras cada transacción
- Controlar el estado de la cuenta (ACTIVA, INACTIVA, BLOQUEADA)
- Permitir al cliente bloquear y desbloquear su propia cuenta (previa verificación de contraseña)
- Cerrar cuentas con saldo cero (previa verificación de contraseña)

## Lo que NO hace

- No ejecuta movimientos de dinero directamente — eso es responsabilidad del módulo de transacciones
- No crea cuentas manualmente desde la API — se crean automáticamente al registrar un cliente (HU-02, Sprint 2)

## Modelo de datos

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id_cuenta` | `BIGINT` PK | Identificador único |
| `numero_cuenta` | `VARCHAR(20)` UNIQUE | Número de 10 dígitos (prefijo 500 para ahorros) |
| `tipo` | `AHORROS` / `CORRIENTE` | Tipo de cuenta |
| `saldo` | `DECIMAL(19,4)` | Saldo disponible. Siempre `BigDecimal` en código. |
| `estado` | `ACTIVA` / `INACTIVA` | Estado actual de la cuenta |
| `id_cliente` | FK → `cliente` | Propietario |

## Estados de la cuenta

```
ACTIVA ──── bloquear ────▶ BLOQUEADA ──── desbloquear ────▶ ACTIVA
ACTIVA ──── cerrar   ────▶ INACTIVA
```

| Estado | Puede operar | Descripción |
|--------|-------------|-------------|
| `ACTIVA` | Sí | Estado normal |
| `BLOQUEADA` | No | Bloqueada preventivamente por el cliente |
| `INACTIVA` | No | Cerrada definitivamente |

## Endpoints de seguridad de cuenta

| Método | Ruta | Descripción | Auth requerida |
|--------|------|-------------|----------------|
| `POST` | `/api/v1/cuentas/seguridad/bloquear` | Bloquear cuenta del usuario autenticado | Sí |
| `POST` | `/api/v1/cuentas/seguridad/desbloquear` | Desbloquear cuenta del usuario autenticado | Sí |
| `PATCH` | `/api/v1/cuentas/cerrar` | Cerrar cuenta (requiere saldo cero) | Sí |
| `GET` | `/api/v1/cuentas/dashboard` | Ver todas las cuentas del usuario | Sí |

> **El ID del usuario nunca se acepta desde el cuerpo de la petición.** Los endpoints de bloqueo/desbloqueo y cierre operan siempre sobre la cuenta del usuario autenticado (identificado por el JWT). Solo se solicita la contraseña como confirmación.

## Dependencias

| Módulo / Clase | Para qué |
|----------------|----------|
| `CuentaRepository` | Consultar y actualizar cuentas |
| `TransaccionRepository` | Registrar movimientos asociados a la cuenta |
| `CuentaMapper` | Convertir `Cuenta` → `CuentaDTO` |
