# Task 59 y Task 60 - Registro y modelo de transacciones

## Task 59 - Registrar transacción

**Entregable:** Inserción en tabla transacciones.

**Criterio:** Registro con fecha, monto y tipo `DEPÓSITO`.

La implementación existente de depósitos se mantiene en:

`POST /api/v1/transacciones/depositar`

Al ejecutar un depósito:

- Se incrementa el saldo de la cuenta.
- Se inserta una transacción.
- Se registra fecha.
- Se registra monto.
- Se registra tipo de operación `DEPOSITO`.
- Se deja estado `EXITOSA`.

## Task 60 - Ajustar modelo de transacciones

**Entregable:** Tabla/campo con `tipo_operacion`.

**Criterio:** Permite registrar depósitos.

Se ajustó la entidad `Transaccion` para mapear el tipo de transacción sobre la columna:

```java
@Column(name = "tipo_operacion", nullable = false)
private TipoTransaccion tipo;
```

Además se amplió el enum `TipoTransaccion` para soportar:

- `DEPOSITO`
- `RETIRO`
- `TRANSFERENCIA`
- `TRANSFERENCIA_INTERBANCARIA`
- `REVERSO_ACH`

Y se amplió el estado de transacciones para soportar flujos ACH:

- `EXITOSA`
- `FALLIDA`
- `PENDIENTE_PROCESAMIENTO`
- `RECHAZADA`
- `REVERSADA`
