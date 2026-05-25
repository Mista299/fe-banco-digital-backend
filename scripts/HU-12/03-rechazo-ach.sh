#!/bin/bash
# Escenario 3: flujo rechazo ACH — el saldo debe reversarse al emisor
# Esperado: creación OK, rechazo 200, saldo vuelve al valor original

source "$(dirname "$0")/_comun.sh"

echo "=== HU-12 Escenario 3: rechazo ACH con reversión de saldo ==="

login "bryan" "bryan123"
ID=$(get_id_cuenta)
SALDO_ANTES=$(get_saldo)
echo "  idCuenta: $ID  saldo antes: $SALDO_ANTES"

RESP=$(crear_transferencia "$ID" 50000)
ID_TX=$(echo "$RESP" | jq -r '.idTransaccion // empty')

[ -n "$ID_TX" ] && [ "$ID_TX" != "null" ] \
  && ok "Transferencia creada — idTransaccion: $ID_TX" \
  || { fail "No se obtuvo idTransaccion"; resumen; exit 1; }

echo ""
echo "--- Registrando rechazo ACH (id=$ID_TX) ---"
RECHAZO=$(curl -s -w "\n%{http_code}" -b "$COOKIES" \
  -X POST "${TRANSFER_URL}/${ID_TX}/rechazo-ach" \
  -H "Content-Type: application/json" \
  -H "X-Gateway-Secret: $GATEWAY_SECRET" \
  -d '{"motivo": "Cuenta destino invalida en red ACH (codigo ACH_01)"}')

HTTP_RECH=$(echo "$RECHAZO" | tail -1)
echo "$RECHAZO" | head -n -1 | jq . 2>/dev/null || echo "$RECHAZO"

[ "$HTTP_RECH" = "200" ] \
  && ok "Rechazo ACH: HTTP 200" \
  || fail "Rechazo ACH esperado 200, obtenido: $HTTP_RECH"

SALDO_DESPUES=$(get_saldo)
echo "  saldo después del rechazo: $SALDO_DESPUES"

REVERSADO=$(python3 -c "print(abs($SALDO_ANTES - $SALDO_DESPUES) < 0.01)")
[ "$REVERSADO" = "True" ] \
  && ok "Saldo reversado — igual al original ($SALDO_DESPUES)" \
  || fail "Saldo no reversado: antes=$SALDO_ANTES después=$SALDO_DESPUES"

resumen
