#!/bin/bash
# Escenario 1: retiro exitoso con saldo suficiente
# Esperado: 200 OK con comprobante (tipo=RETIRO, estado=EXITOSO, saldoResultante)

source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 1: retiro exitoso ==="

# Registrar usuario fresco para tener saldo controlado
registrar_usuario "R1"
login "$USUARIO_REG" "$PASS_REG"
ID=$(get_id_cuenta)

# Depositar primero para tener saldo
COOKIES_TMP="$COOKIES"
curl -s -b "$COOKIES_TMP" -X POST "${URL_BASE}/transacciones/depositar" \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$ID,\"monto\":200000}" > /dev/null

http_code=$(retirar "$ID" 50000)

[ "$http_code" = "200" ] && ok "HTTP 200 OK" || fail "Esperado 200, obtenido: $http_code"

TIPO=$(jq -r '.tipo // empty' "$_BODY_FILE")
[ "$TIPO" = "RETIRO" ] && ok "tipo = RETIRO" || fail "tipo esperado RETIRO, obtenido '$TIPO'"

ESTADO=$(jq -r '.estado // empty' "$_BODY_FILE")
[ "$ESTADO" = "EXITOSO" ] && ok "estado = EXITOSO" || fail "estado esperado EXITOSO, obtenido '$ESTADO'"

ID_TX=$(jq -r '.idTransaccion // empty' "$_BODY_FILE")
[ -n "$ID_TX" ] && [ "$ID_TX" != "null" ] && ok "idTransaccion presente: $ID_TX" || fail "idTransaccion ausente"

resumen
