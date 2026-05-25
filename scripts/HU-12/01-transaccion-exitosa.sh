#!/bin/bash
# Escenario 1: flujo completo — crear transferencia + confirmar ACH
# Esperado: creación 200, confirmación 200

source "$(dirname "$0")/_comun.sh"

echo "=== HU-12 Escenario 1: transferencia interbancaria exitosa ==="

login "bryan" "bryan123"
ID=$(get_id_cuenta)
SALDO_ANTES=$(get_saldo)
echo "  idCuenta: $ID  saldo: $SALDO_ANTES"

echo ""
echo "--- Creando transferencia interbancaria ---"
RESP=$(crear_transferencia "$ID" 50000)
echo "$RESP" | jq . 2>/dev/null || echo "$RESP"

HTTP_CREATE=$(echo "$RESP" | jq -r '.estado // empty')
ID_TX=$(echo "$RESP" | jq -r '.idTransaccion // empty')

[ -n "$ID_TX" ] && [ "$ID_TX" != "null" ] \
  && ok "Transferencia creada — idTransaccion: $ID_TX" \
  || fail "No se obtuvo idTransaccion en la respuesta"

echo ""
echo "--- Confirmando ACH (id=$ID_TX) ---"
CONFIRM=$(curl -s -w "\n%{http_code}" -b "$COOKIES" \
  -X POST "${TRANSFER_URL}/${ID_TX}/confirmacion-ach" \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Secret: $GATEWAY_SECRET" \
  -d '{"referenciaConfirmacion": "ACH-CONFIRM-OK"}')

HTTP_CONF=$(echo "$CONFIRM" | tail -1)
echo "$CONFIRM" | head -n -1 | jq . 2>/dev/null || echo "$CONFIRM"

[ "$HTTP_CONF" = "200" ] \
  && ok "Confirmación ACH: HTTP 200" \
  || fail "Confirmación ACH esperada 200, obtenida: $HTTP_CONF"

resumen
