# Casos de prueba - Iteration 2

## HU-13 - Motor de Validación

### Caso 1: Validación exitosa

**Dado** que la cuenta está `ACTIVA` y tiene saldo suficiente.

**Cuando** se llama `POST /api/v1/validaciones/transaccion`.

**Entonces** retorna `autorizada=true` y código `AUTORIZADA`.

### Caso 2: Fondos insuficientes

**Dado** que el monto solicitado supera el saldo disponible.

**Cuando** se ejecuta la validación.

**Entonces** retorna `autorizada=false`, código `SALDO_INSUFICIENTE` y mensaje de saldo insuficiente.

### Caso 3: Cuenta inactiva

**Dado** que la cuenta origen está `INACTIVA`.

**Cuando** se ejecuta la validación.

**Entonces** retorna `CUENTA_INACTIVA` y bloquea movimientos de salida.

### Caso 4: Cuenta bloqueada

**Dado** que la cuenta origen está `BLOQUEADA`.

**Cuando** se ejecuta la validación.

**Entonces** retorna `CUENTA_BLOQUEADA` y no permite débito.

## HU-12 - Transferencias interbancarias

### Caso 1: Envío exitoso ACH

**Dado** que el usuario tiene saldo suficiente y cuenta activa.

**Cuando** llama `POST /api/v1/transferencias/interbancarias`.

**Entonces** se debita el saldo, se crea una transacción `TRANSFERENCIA_INTERBANCARIA` y queda `PENDIENTE_PROCESAMIENTO`.

### Caso 2: Rechazo ACH post-envío

**Dado** que existe una transacción interbancaria pendiente.

**Cuando** se llama `POST /api/v1/transferencias/interbancarias/{id}/rechazo-ach`.

**Entonces** se reversa el valor al saldo del emisor y la transacción original queda `REVERSADA`.

### Caso 3: Rechazo preventivo por reglas

**Dado** que el Motor de Validación retorna saldo insuficiente, cuenta bloqueada o inactiva.

**Cuando** se intenta iniciar la transferencia interbancaria.

**Entonces** se interrumpe el flujo y no se genera orden ACH.
