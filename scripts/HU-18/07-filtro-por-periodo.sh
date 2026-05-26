#!/bin/bash
# HU-18 · Escenario 3 — Filtro por rango de fechas
#
# Valida:
#   - Con fechaInicio+fechaFin en 2026 se devuelven movimientos del seed
#   - Todos los movimientos devueltos están dentro del rango indicado
#   - Rango futuro (2099) devuelve lista vacía
#   - Combinación de fecha + tipo funciona correctamente

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-18 · Escenario 3 — Filtro por período"
echo "========================================================"

echo "--- Login como bryan (rol ADMIN) ---"
login "bryan" "bryan123" "$COOKIES_ADMIN"

INICIO_OK="2026-01-01T00:00:00"
FIN_OK="2026-12-31T23:59:59"

# ── Rango válido con datos ────────────────────────────────────────────────────
echo ""
echo "--- Rango 2026 completo (debe contener transacciones del seed) ---"
RESP=$(buscar_doc "$DOC_ANA" "$INICIO_OK" "$FIN_OK" "" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK (rango válido)" \
  || fail "Esperado 200, obtenido: $HTTP"

LEN=$(echo "$BODY" | jq '.movimientos | length' 2>/dev/null)
[ -n "$LEN" ] && [ "$LEN" -gt 0 ] \
  && ok "Rango 2026 devuelve $LEN movimiento(s)" \
  || fail "Rango 2026 no devolvió movimientos (esperado >0 por datos del seed)"

# Todos los movimientos deben estar dentro del rango
if [ -n "$LEN" ] && [ "$LEN" -gt 0 ]; then
  RESULTADO=$(python3 - <<PYEOF
import json, sys
from datetime import datetime
body = json.loads("""$(echo "$BODY" | jq -c '.' | sed 's/"/\\"/g' | sed 's/\\\\"/"/g')""")
inicio = datetime.fromisoformat("$INICIO_OK")
fin    = datetime.fromisoformat("$FIN_OK")
fuera  = [m["fechaHora"] for m in body["movimientos"]
          if not (inicio <= datetime.fromisoformat(m["fechaHora"]) <= fin)]
print("FUERA:" + str(fuera) if fuera else "TODOS_DENTRO")
PYEOF
)
  [ "$RESULTADO" = "TODOS_DENTRO" ] \
    && ok "Todos los movimientos están dentro del rango $INICIO_OK / $FIN_OK" \
    || fail "Movimientos fuera del rango: $RESULTADO"
fi

# ── Rango futuro sin datos ────────────────────────────────────────────────────
echo ""
echo "--- Rango 2099 (sin transacciones) ---"
RESP=$(buscar_doc "$DOC_ANA" "2099-01-01T00:00:00" "2099-12-31T23:59:59" "" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK (rango futuro)" \
  || fail "Esperado 200, obtenido: $HTTP"

LEN=$(echo "$BODY" | jq '.movimientos | length' 2>/dev/null)
[ "$LEN" = "0" ] \
  && ok "Rango futuro devuelve 0 movimientos" \
  || fail "Esperado 0 movimientos en rango futuro, obtenido: $LEN"

# ── Combinación fecha + tipo ──────────────────────────────────────────────────
echo ""
echo "--- Rango 2026 + tipo=DEPOSITO ---"
RESP=$(buscar_doc "$DOC_ANA" "$INICIO_OK" "$FIN_OK" "DEPOSITO" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK (fecha + tipo)" \
  || fail "Esperado 200 con fecha+tipo, obtenido: $HTTP"

LEN=$(echo "$BODY" | jq '.movimientos | length' 2>/dev/null)
[ -n "$LEN" ] && [ "$LEN" -gt 0 ] \
  && ok "Combinación fecha+tipo devuelve $LEN movimiento(s)" \
  || fail "Combinación fecha+tipo no devolvió movimientos"

TODOS=$(echo "$BODY" | jq '[.movimientos[] | .concepto | contains("DEPOSITO")] | all' 2>/dev/null)
[ "$TODOS" = "true" ] \
  && ok "Todos los movimientos son DEPOSITO dentro del rango de fechas" \
  || fail "Hay movimientos con concepto distinto a DEPOSITO en el filtro combinado"

rm -f "$COOKIES_ADMIN"
resumen
