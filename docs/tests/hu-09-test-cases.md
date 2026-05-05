# Casos de prueba — HU-09 Depósito vía Pasarela con Autenticación HMAC

## CP-01 Depósito exitoso en cuenta activa

**Dado** una cuenta con estado `ACTIVA` y una firma HMAC válida.
**Cuando** la pasarela envía `POST /api/v1/depositos/notificacion`.
**Entonces** el sistema retorna `200` con el comprobante: número de operación, fecha, monto, número de cuenta destino y saldo resultante actualizado.

Script: `scripts/HU-09/01-deposito-exitoso.sh`

---

## CP-02 Cuenta destino no encontrada

**Dado** un número de cuenta que no existe en la base de datos y una firma HMAC válida.
**Cuando** la pasarela envía la notificación.
**Entonces** el sistema retorna `422 Unprocessable Entity` con `motivo: "Cuenta no encontrada."` y `devolucionSimulada: true`.

Script: `scripts/HU-09/02-cuenta-no-encontrada.sh`

---

## CP-03 Cuenta destino inactiva (cerrada)

**Dado** una cuenta con estado `INACTIVA` y una firma HMAC válida.
**Cuando** la pasarela envía la notificación.
**Entonces** el sistema retorna `422` con `motivo: "Cuenta cerrada."` y `devolucionSimulada: true`.

Script: `scripts/HU-09/03-cuenta-inactiva.sh`

---

## CP-04 Cuenta destino bloqueada

**Dado** una cuenta con estado `BLOQUEADA` y una firma HMAC válida.
**Cuando** la pasarela envía la notificación.
**Entonces** el sistema retorna `422` con `motivo: "Cuenta bloqueada."` y `devolucionSimulada: true`.

Script: `scripts/HU-09/04-cuenta-bloqueada.sh`
> Prerequisito: ejecutar `UPDATE cuenta SET estado = 'BLOQUEADA' WHERE numero_cuenta = '00020001';`

---

## CP-05 Campos faltantes en el body

**Dado** un body sin el campo `monto` y una firma HMAC válida calculada sobre ese body.
**Cuando** la pasarela envía la notificación.
**Entonces** el sistema retorna `400 Bad Request` con el detalle de los campos faltantes.

Script: `scripts/HU-09/05-campos-faltantes.sh`

---

## CP-06 Monto inválido (cero o negativo)

**Dado** un body con `monto: 0` y una firma HMAC válida.
**Cuando** la pasarela envía la notificación.
**Entonces** el sistema retorna `400 Bad Request` indicando que el monto debe ser mayor a cero (`@DecimalMin(0.01)`).

Script: `scripts/HU-09/06-monto-invalido.sh`

---

## CP-07 Header de firma ausente

**Dado** un request sin el header `X-Gateway-Signature`.
**Cuando** la pasarela envía la notificación.
**Entonces** el sistema retorna `401 Unauthorized` con `mensaje: "Firma HMAC ausente."`. El request no llega al controlador.

Script: `scripts/HU-09/07-sin-firma.sh`

---

## CP-08 Firma inválida

**Dado** un request con `X-Gateway-Signature` presente pero con un valor incorrecto.
**Cuando** la pasarela envía la notificación.
**Entonces** el sistema retorna `401 Unauthorized` con `mensaje: "Firma HMAC inválida."`. El request no llega al controlador.

Script: `scripts/HU-09/08-firma-invalida.sh`
