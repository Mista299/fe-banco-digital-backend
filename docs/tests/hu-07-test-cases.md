# Casos de prueba — HU-07 Dashboard de Cuentas

## CP-01 Múltiples cuentas activas

**Dado** un cliente autenticado con más de una cuenta.
**Cuando** llama `GET /api/v1/cuentas/dashboard`.
**Entonces** el sistema retorna `200` con una lista donde cada elemento tiene `numeroEnmascarado` con formato `****XXXX`, `tipo`, `saldo` y `saldoDisponible: true`.

Script: `scripts/HU-07-dashboard/01-multiples-cuentas.sh`

---

## CP-02 Cuenta única

**Dado** un cliente autenticado con una sola cuenta activa.
**Cuando** llama `GET /api/v1/cuentas/dashboard`.
**Entonces** el sistema retorna `200` con una lista de exactamente un elemento con el detalle completo de esa cuenta.

Script: `scripts/HU-07-dashboard/02-cuenta-unica.sh`

---

## CP-03 Mezcla de cuentas activas e inactivas

**Dado** un cliente con al menos una cuenta `ACTIVA` y una `INACTIVA`.
**Cuando** llama `GET /api/v1/cuentas/dashboard`.
**Entonces** la cuenta `INACTIVA` aparece con `etiquetaVisual: "Cuenta Cerrada"` y `permiteTransacciones: false`. La cuenta `ACTIVA` tiene `etiquetaVisual: null` y `permiteTransacciones: true`.

---

## CP-04 Error de servidor (Escenario 3)

**Dado** que el servidor no está disponible.
**Cuando** el cliente intenta acceder al dashboard.
**Entonces** no se recibe respuesta JSON — el frontend debe mostrar "Dato no disponible" en el campo de saldo y ofrecer el botón "Reintentar carga".

Script: `scripts/HU-07-dashboard/03-error-servidor.sh`

---

## CP-05 Sin autenticación

**Dado** que el usuario no tiene sesión activa (sin cookie `accessToken`).
**Cuando** llama `GET /api/v1/cuentas/dashboard`.
**Entonces** el sistema retorna `401 Unauthorized`.

Script: `scripts/HU-07-dashboard/04-sin-autenticacion.sh`
