#!/bin/bash
# HU-17 · Escenario 1 — Consolidado global de liquidez
#
# Valida:
#   - HTTP 200 para ADMIN
#   - Campos totalSistema, totalAhorros, totalCorriente presentes
#   - totalSistema = totalAhorros + totalCorriente
#   - Todos los valores son numéricos positivos o cero
#
# Saldos seed:
#   AHORROS:   00010001(550000) + 00020001(745000) + 00030001(800000)
#            + 00050001(0)      + 00060001(620000)  = 2715000
#   CORRIENTE: 00040001(100000) + 00010002(1350000) + 00030002(540000) = 1990000
#   TOTAL:     4705000

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-17 · Escenario 1 — Consolidado global de liquidez"
echo "========================================================"

echo "--- Login como bryan (rol ADMIN) ---"
login "bryan" "bryan123" "$COOKIES_ADMIN"

echo ""
echo "--- GET /reportes/saldos/consolidado ---"
RESP=$(get_reporte "consolidado" "" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK" \
  || fail "Esperado 200, obtenido: $HTTP"

# ── Campos presentes ──────────────────────────────────────────────────────────
TOTAL=$(echo "$BODY" | jq -r '.totalSistema // empty' 2>/dev/null)
AHORROS=$(echo "$BODY" | jq -r '.totalAhorros // empty' 2>/dev/null)
CORRIENTE=$(echo "$BODY" | jq -r '.totalCorriente // empty' 2>/dev/null)

[ -n "$TOTAL" ]     && ok "Campo totalSistema presente: $TOTAL"     || fail "Campo totalSistema ausente"
[ -n "$AHORROS" ]   && ok "Campo totalAhorros presente: $AHORROS"   || fail "Campo totalAhorros ausente"
[ -n "$CORRIENTE" ] && ok "Campo totalCorriente presente: $CORRIENTE" || fail "Campo totalCorriente ausente"

# ── Valores positivos ─────────────────────────────────────────────────────────
python3 -c "print(float('${TOTAL:-0}') >= 0)" 2>/dev/null | grep -q "True" \
  && ok "totalSistema es >= 0" \
  || fail "totalSistema negativo: $TOTAL"

# ── Coherencia: totalSistema = totalAhorros + totalCorriente ──────────────────
SUMA=$(python3 -c "print(round(float('${AHORROS:-0}') + float('${CORRIENTE:-0}'), 4))" 2>/dev/null)
TOTAL_R=$(python3 -c "print(round(float('${TOTAL:-0}'), 4))" 2>/dev/null)
[ "$SUMA" = "$TOTAL_R" ] \
  && ok "totalSistema ($TOTAL_R) = totalAhorros + totalCorriente ($SUMA)" \
  || fail "Incoherencia: totalSistema=$TOTAL_R pero ahorros+corriente=$SUMA"

# ── Valores del seed ─────────────────────────────────────────────────────────
python3 -c "print(float('${AHORROS:-0}') >= 2715000)" 2>/dev/null | grep -q "True" \
  && ok "totalAhorros ($AHORROS) ≥ saldo seed AHORROS (2715000)" \
  || fail "totalAhorros ($AHORROS) < saldo seed esperado (2715000)"

python3 -c "print(float('${CORRIENTE:-0}') >= 1990000)" 2>/dev/null | grep -q "True" \
  && ok "totalCorriente ($CORRIENTE) ≥ saldo seed CORRIENTE (1990000)" \
  || fail "totalCorriente ($CORRIENTE) < saldo seed esperado (1990000)"

rm -f "$COOKIES_ADMIN"
resumen
