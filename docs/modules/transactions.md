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

## Endpoints de consulta

| Método | Ruta | Descripción | Auth requerida |
|--------|------|-------------|----------------|
| `GET` | `/api/v1/transacciones/cuenta/{idCuenta}` | Movimientos de una cuenta | Sí |
| `GET` | `/api/v1/transacciones/cuenta/{idCuenta}/filtro` | Movimientos filtrados por fechas | Sí |

> **Verificación de propiedad:** antes de devolver movimientos, el servicio verifica que la cuenta `{idCuenta}` pertenezca al usuario autenticado. Si no pertenece, responde `403 Forbidden`.

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
