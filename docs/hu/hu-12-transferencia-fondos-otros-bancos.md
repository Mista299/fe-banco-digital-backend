# HU-12 - Transferencia de fondos a otros bancos nacionales

## Historia de usuario

Como cliente del banco digital, quiero realizar transferencias de dinero hacia cuentas de ahorros o corrientes en otros bancos, para cumplir con mis obligaciones financieras y pagos a personas que no pertenecen a mi misma entidad.

## Implementación backend agregada

### Endpoint principal

`POST /api/v1/transferencias/interbancarias`

Crea una orden interbancaria tipo ACH. Antes de debitar, consulta el Motor de Validación para confirmar saldo suficiente y cuenta activa.

### Endpoint de rechazo ACH

`POST /api/v1/transferencias/interbancarias/{idTransaccion}/rechazo-ach`

Simula el rechazo reportado por la red ACH o banco destino. El sistema reversa automáticamente el valor al saldo de la cuenta origen y marca la transacción original como `REVERSADA`.

## Escenarios cubiertos

### Escenario 1: Envío exitoso a otro banco - proceso ACH

- Valida saldo y estado de cuenta.
- Debita el monto de la cuenta origen.
- Registra la transacción con tipo `TRANSFERENCIA_INTERBANCARIA`.
- Deja el estado en `PENDIENTE_PROCESAMIENTO`.
- Genera referencia externa `ACH-*`.

### Escenario 2: Rechazo por datos del receptor incorrectos - post-envío

- Recibe un motivo de rechazo.
- Suma el valor íntegro al saldo del emisor.
- Marca la transacción original como `REVERSADA`.
- Registra una transacción adicional tipo `REVERSO_ACH`.

### Escenario 3: Rechazo preventivo por reglas de negocio

- Si el Motor de Validación retorna `SALDO_INSUFICIENTE`, `CUENTA_BLOQUEADA` o `CUENTA_INACTIVA`, no se genera orden ACH.
- Se registra una transacción fallida para trazabilidad.
- Se devuelve el mensaje de alerta correspondiente.

## Archivos principales

- `TransferenciaInterbancariaController.java`
- `TransferenciaInterbancariaService.java`
- `TransferenciaInterbancariaServiceImpl.java`
- `TransferenciaInterbancariaSolicitudDTO.java`
- `TransferenciaInterbancariaResponseDTO.java`
- `RechazoAchSolicitudDTO.java`
