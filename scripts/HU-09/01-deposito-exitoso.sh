#!/bin/bash
# Escenario 1: depósito exitoso en cuenta ACTIVA
# Esperado: 200 OK con comprobante (tipo=DEPOSITO, estado=EXITOSO, saldoResultante)

source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 1: depósito exitoso ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)

http_code=$(depositar "$ID" 500000)

if [ "$http_code" = "200" ]; then
  ok "HTTP 200 OK"
else
  fail "Esperado 200, obtenido: $http_code"
fi

TIPO=$(jq -r '.tipo // empty' "$_BODY_FILE")
[ "$TIPO" = "DEPOSITO" ] && ok "tipo = DEPOSITO" || fail "tipo esperado DEPOSITO, obtenido '$TIPO'"

ESTADO=$(jq -r '.estado // empty' "$_BODY_FILE")
[ "$ESTADO" = "EXITOSO" ] && ok "estado = EXITOSO" || fail "estado esperado EXITOSO, obtenido '$ESTADO'"

ID_TX=$(jq -r '.idTransaccion // empty' "$_BODY_FILE")
[ -n "$ID_TX" ] && [ "$ID_TX" != "null" ] && ok "idTransaccion presente: $ID_TX" || fail "idTransaccion ausente"

SALDO=$(jq -r '.saldoResultante // empty' "$_BODY_FILE")
[ -n "$SALDO" ] && [ "$SALDO" != "null" ] && ok "saldoResultante presente: $SALDO" || fail "saldoResultante ausente"

resumen
