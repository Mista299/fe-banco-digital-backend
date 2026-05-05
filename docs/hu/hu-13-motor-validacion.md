# HU-13 - Motor de Validación

## Historia de usuario

Como Administrador, quiero que el sistema valide automáticamente el saldo y el estado de la cuenta para garantizar que no se realicen transacciones sin fondos o en cuentas cerradas o bloqueadas por seguridad para proteger el patrimonio del banco y del cliente.

## Endpoint agregado

`POST /api/v1/validaciones/transaccion`

Valida si una operación de salida puede continuar según saldo disponible y estado de la cuenta.

## Reglas implementadas

### 1. Validación exitosa

Si la cuenta está `ACTIVA` y tiene saldo suficiente, retorna:

- `autorizada = true`
- `codigo = AUTORIZADA`

### 2. Rechazo por fondos insuficientes

Si el monto solicitado es mayor al saldo disponible, retorna:

- `autorizada = false`
- `codigo = SALDO_INSUFICIENTE`
- Mensaje: `Saldo insuficiente para completar esta operación.`

### 3. Rechazo por cuenta cerrada o inactiva

Si la cuenta está `INACTIVA`, retorna:

- `autorizada = false`
- `codigo = CUENTA_INACTIVA`
- Mensaje: `La cuenta de origen no se encuentra habilitada para realizar transacciones.`

### 4. Rechazo por bloqueo de seguridad o embargo

Si la cuenta está `BLOQUEADA`, retorna:

- `autorizada = false`
- `codigo = CUENTA_BLOQUEADA`
- Mensaje: `Operación no permitida. Por razones de seguridad, su cuenta presenta una restricción activa.`

## Archivos principales

- `MotorValidacionController.java`
- `MotorValidacionService.java`
- `MotorValidacionServiceImpl.java`
- `ValidacionTransaccionSolicitudDTO.java`
- `ValidacionTransaccionResponseDTO.java`
