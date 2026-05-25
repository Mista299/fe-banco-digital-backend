#!/bin/bash
# Escenario 2: crear transferencia y verificar campos de la respuesta
# Esperado: 200 con idTransaccion, estado PENDIENTE

source "$(dirname "$0")/_comun.sh"

echo "=== HU-12 Escenario 2: campos de transferencia creada ==="

login "bryan" "bryan123"
ID=$(get_id_cuenta)

RESP=$(crear_transferencia "$ID" 30000)
echo "$RESP" | jq . 2>/dev/null || echo "$RESP"

ID_TX=$(echo "$RESP" | jq -r '.idTransaccion // empty')
[ -n "$ID_TX" ] && [ "$ID_TX" != "null" ] \
  && ok "idTransaccion presente: $ID_TX" \
  || fail "idTransaccion ausente"

ESTADO=$(echo "$RESP" | jq -r '.estado // empty')
[ "$ESTADO" = "PENDIENTE_PROCESAMIENTO" ] \
  && ok "estado = PENDIENTE_PROCESAMIENTO" \
  || fail "estado esperado PENDIENTE_PROCESAMIENTO, obtenido: '$ESTADO'"

MONTO=$(echo "$RESP" | jq -r '.monto // empty')
[ -n "$MONTO" ] && [ "$MONTO" != "null" ] \
  && ok "monto presente: $MONTO" \
  || fail "monto ausente"

resumen
