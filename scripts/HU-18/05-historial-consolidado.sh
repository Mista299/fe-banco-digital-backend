#!/bin/bash
# HU-18 · Escenario 2 — Visualización del historial consolidado
#
# Valida:
#   - movimientos[] presente y no vacío para cliente con transacciones
#   - Cada movimiento tiene: idTransaccion, fechaHora, concepto, monto, saldoResultante
#   - saldoResultante no es null (Escenario 2 explícito de la HU)
#   - totalMovimientos coincide con el tamaño del array
#   - Movimientos ordenados de más reciente a más antiguo

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-18 · Escenario 2 — Historial consolidado"
echo "========================================================"

echo "--- Login como bryan (rol ADMIN) ---"
login "bryan" "bryan123" "$COOKIES_ADMIN"

echo ""
echo "--- GET /buscar/documento?documento=$DOC_ANA (Ana, múltiples transacciones) ---"
RESP=$(buscar_doc "$DOC_ANA" "" "" "" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)
echo ""

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK" \
  || fail "Esperado 200, obtenido: $HTTP"

LEN=$(echo "$BODY" | jq '.movimientos | length' 2>/dev/null)
[ -n "$LEN" ] && [ "$LEN" != "null" ] && [ "$LEN" -gt 0 ] \
  && ok "movimientos[] presente con $LEN registros" \
  || fail "movimientos[] ausente o vacío (len=$LEN)"

# totalMovimientos debe coincidir con el tamaño del array
TOTAL=$(echo "$BODY" | jq -r '.totalMovimientos // 0')
MATCH=$(python3 -c "print($TOTAL == $LEN)" 2>/dev/null)
[ "$MATCH" = "True" ] \
  && ok "totalMovimientos ($TOTAL) coincide con tamaño del array ($LEN)" \
  || fail "Discrepancia: totalMovimientos=$TOTAL pero array tiene $LEN elementos"

# Primer movimiento — verificar todos los campos requeridos
echo ""
echo "  Primer movimiento:"
echo "$BODY" | jq '.movimientos[0]' 2>/dev/null
echo ""

ID=$(echo "$BODY" | jq -r '.movimientos[0].idTransaccion // empty')
[ -n "$ID" ] && [ "$ID" != "null" ] \
  && ok "movimiento.idTransaccion presente: $ID" \
  || fail "movimiento.idTransaccion ausente"

FECHA=$(echo "$BODY" | jq -r '.movimientos[0].fechaHora // empty')
[ -n "$FECHA" ] && [ "$FECHA" != "null" ] \
  && ok "movimiento.fechaHora presente: $FECHA" \
  || fail "movimiento.fechaHora ausente"

CONCEPTO=$(echo "$BODY" | jq -r '.movimientos[0].concepto // empty')
[ -n "$CONCEPTO" ] && [ "$CONCEPTO" != "null" ] \
  && ok "movimiento.concepto presente: $CONCEPTO" \
  || fail "movimiento.concepto ausente"

MONTO=$(echo "$BODY" | jq -r '.movimientos[0].monto // empty')
[ -n "$MONTO" ] && [ "$MONTO" != "null" ] \
  && ok "movimiento.monto presente: $MONTO" \
  || fail "movimiento.monto ausente"

# saldoResultante — requerimiento explícito del Escenario 2
SALDO=$(echo "$BODY" | jq -r '.movimientos[0].saldoResultante // empty')
[ -n "$SALDO" ] && [ "$SALDO" != "null" ] \
  && ok "movimiento.saldoResultante presente: $SALDO (Escenario 2 cumplido)" \
  || fail "movimiento.saldoResultante ausente o null"

# Verificar que ningún movimiento tiene saldoResultante null
NULL_COUNT=$(echo "$BODY" | jq '[.movimientos[] | select(.saldoResultante == null)] | length' 2>/dev/null)
[ "$NULL_COUNT" = "0" ] \
  && ok "Todos los movimientos tienen saldoResultante no-null" \
  || fail "$NULL_COUNT movimiento(s) con saldoResultante null"

# Verificar orden cronológico (más reciente primero) cuando hay ≥2 movimientos
if [ -n "$LEN" ] && [ "$LEN" -ge 2 ]; then
  FECHA1=$(echo "$BODY" | jq -r '.movimientos[0].fechaHora')
  FECHA2=$(echo "$BODY" | jq -r '.movimientos[1].fechaHora')
  ORDEN=$(python3 -c "
from datetime import datetime
f1 = datetime.fromisoformat('${FECHA1}')
f2 = datetime.fromisoformat('${FECHA2}')
print(f1 >= f2)
" 2>/dev/null)
  [ "$ORDEN" = "True" ] \
    && ok "Orden cronológico correcto: movimientos[0] ($FECHA1) >= movimientos[1] ($FECHA2)" \
    || fail "Orden cronológico incorrecto: $FECHA1 debería ser >= $FECHA2"
fi

rm -f "$COOKIES_ADMIN"
resumen
