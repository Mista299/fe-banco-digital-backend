#!/bin/bash
# Escenario 9: verificar que el saldo del dashboard se actualiza tras el depósito
# Esperado: saldo_después = saldo_antes + monto

source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 9: saldo actualizado post-depósito ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)
MONTO=75000

SALDO_ANTES=$(get_saldo)
echo "  Saldo antes: $SALDO_ANTES"

http_code=$(depositar "$ID" "$MONTO")
[ "$http_code" = "200" ] && ok "Depósito aceptado (200)" || fail "Depósito rechazado: $http_code"

SALDO_DESPUES=$(get_saldo)
echo "  Saldo después: $SALDO_DESPUES"

CORRECTO=$(python3 -c "print(abs($SALDO_DESPUES - $SALDO_ANTES - $MONTO) < 0.01)")
[ "$CORRECTO" = "True" ] \
  && ok "Saldo incrementado en exactamente \$$MONTO" \
  || fail "Saldo incorrecto: antes=$SALDO_ANTES después=$SALDO_DESPUES (esperaba +$MONTO)"

resumen
