# Módulo de Cuentas

## ¿Qué hace?

Representa las cuentas bancarias de los clientes. Gestiona su estado, tipo y saldo.

## Responsabilidades

- Asociar cuentas a clientes (relación N:1)
- Mantener el saldo actualizado tras cada transacción
- Controlar el estado de la cuenta (ACTIVA, INACTIVA)

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
ACTIVA ──── cerrar ────▶ INACTIVA
```

Una cuenta `INACTIVA` no puede recibir ni enviar transacciones.

## Dependencias

| Módulo / Clase | Para qué |
|----------------|----------|
| `CuentaRepository` | Consultar y actualizar cuentas |
| `TransaccionRepository` | Registrar movimientos asociados a la cuenta |
| `CuentaMapper` | Convertir `Cuenta` → `CuentaDTO` |
