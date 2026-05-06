# Entrega Iteration 2 - HU-12, HU-13, Task 59 y Task 60

## Resumen

Se agregan funcionalidades para transacciones, motor de validación y transferencias interbancarias ACH en el backend de Banco Digital EAV-03.

## HU/Tareas incluidas

- HU-12: Transferencia de fondos a otros bancos nacionales.
- HU-13: Motor de Validación.
- Task 59: Registrar transacción.
- Task 60: Ajustar modelo de transacciones.

## Endpoints agregados

### Motor de Validación

`POST /api/v1/validaciones/transaccion`

### Transferencias interbancarias ACH

`POST /api/v1/transferencias/interbancarias`

`POST /api/v1/transferencias/interbancarias/{idTransaccion}/rechazo-ach`

## Cambios técnicos

- Se agrega servicio `MotorValidacionService`.
- Se agrega servicio `TransferenciaInterbancariaService`.
- Se amplía la entidad `Transaccion` con campos de operación ACH.
- Se ajusta el tipo de operación a la columna `tipo_operacion`.
- Se agregan estados para proceso ACH: `PENDIENTE_PROCESAMIENTO`, `REVERSADA`, `RECHAZADA`.
- Se mantiene el endpoint de depósitos para cumplir Task 59.

## Observación

No se elimina funcionalidad previa. Se agregan servicios y endpoints nuevos respetando la arquitectura por capas existente: controller, service, repository, entity, dto y exception.
