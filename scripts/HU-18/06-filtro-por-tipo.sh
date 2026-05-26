#!/bin/bash
# HU-18 · Escenario 3 — Filtro por tipo de movimiento
#
# Valida:
#   - ?tipo=DEPOSITO devuelve solo movimientos con concepto DEPOSITO
#   - ?tipo=RETIRO devuelve solo movimientos RETIRO
#   - ?tipo=TRANSFERENCIA devuelve solo movimientos de tipo transferencia
#   - Tipo inexistente devuelve lista vacía (totalMovimientos = 0)

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-18 · Escenario 3 — Filtro por tipo de movimiento"
echo "========================================================"

echo "--- Login como bryan (rol ADMIN) ---"
login "bryan" "bryan123" "$COOKIES_ADMIN"

# ── DEPOSITO ──────────────────────────────────────────────────────────────────
echo ""
echo "--- GET /buscar/documento?documento=$DOC_ANA&tipo=DEPOSITO ---"
RESP=$(buscar_doc "$DOC_ANA" "" "" "DEPOSITO" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK (tipo=DEPOSITO)" \
  || fail "Esperado 200 con tipo=DEPOSITO, obtenido: $HTTP"

LEN=$(echo "$BODY" | jq '.movimientos | length' 2>/dev/null)
[ -n "$LEN" ] && [ "$LEN" -gt 0 ] \
  && ok "tipo=DEPOSITO devuelve $LEN resultado(s)" \
  || fail "tipo=DEPOSITO no devolvió resultados (esperado ≥1 para Ana)"

TODOS=$(echo "$BODY" | jq '[.movimientos[] | .concepto | contains("DEPOSITO")] | all' 2>/dev/null)
[ "$TODOS" = "true" ] \
  && ok "Todos los movimientos filtrados tienen concepto DEPOSITO" \
  || fail "Hay movimientos con concepto distinto a DEPOSITO en el filtro"

# ── RETIRO ────────────────────────────────────────────────────────────────────
echo ""
echo "--- GET /buscar/documento?documento=$DOC_ANA&tipo=RETIRO ---"
RESP=$(buscar_doc "$DOC_ANA" "" "" "RETIRO" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK (tipo=RETIRO)" \
  || fail "Esperado 200 con tipo=RETIRO, obtenido: $HTTP"

LEN=$(echo "$BODY" | jq '.movimientos | length' 2>/dev/null)
[ -n "$LEN" ] && [ "$LEN" -gt 0 ] \
  && ok "tipo=RETIRO devuelve $LEN resultado(s)" \
  || fail "tipo=RETIRO no devolvió resultados (esperado ≥1 para Ana)"

TODOS=$(echo "$BODY" | jq '[.movimientos[] | .concepto | contains("RETIRO")] | all' 2>/dev/null)
[ "$TODOS" = "true" ] \
  && ok "Todos los movimientos filtrados tienen concepto RETIRO" \
  || fail "Hay movimientos con concepto distinto a RETIRO en el filtro"

# ── TRANSFERENCIA ─────────────────────────────────────────────────────────────
echo ""
echo "--- GET /buscar/documento?documento=$DOC_ANA&tipo=TRANSFERENCIA ---"
RESP=$(buscar_doc "$DOC_ANA" "" "" "TRANSFERENCIA" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK (tipo=TRANSFERENCIA)" \
  || fail "Esperado 200 con tipo=TRANSFERENCIA, obtenido: $HTTP"

LEN=$(echo "$BODY" | jq '.movimientos | length' 2>/dev/null)
[ -n "$LEN" ] && [ "$LEN" -gt 0 ] \
  && ok "tipo=TRANSFERENCIA devuelve $LEN resultado(s)" \
  || fail "tipo=TRANSFERENCIA no devolvió resultados"

TODOS=$(echo "$BODY" | jq '[.movimientos[] | .concepto | contains("TRANSFERENCIA")] | all' 2>/dev/null)
[ "$TODOS" = "true" ] \
  && ok "Todos los movimientos filtrados contienen 'TRANSFERENCIA' en el concepto" \
  || fail "Hay movimientos con concepto sin 'TRANSFERENCIA' en el filtro"

# ── Tipo inexistente ──────────────────────────────────────────────────────────
echo ""
echo "--- GET /buscar/documento?documento=$DOC_ANA&tipo=TIPOINEXISTENTE ---"
RESP=$(buscar_doc "$DOC_ANA" "" "" "TIPOINEXISTENTE" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK con tipo inexistente" \
  || fail "Esperado 200 con tipo inexistente, obtenido: $HTTP"

LEN=$(echo "$BODY" | jq '.movimientos | length' 2>/dev/null)
[ "$LEN" = "0" ] \
  && ok "tipo inexistente devuelve lista vacía (0 movimientos)" \
  || fail "Esperado 0 movimientos para tipo inexistente, obtenido: $LEN"

rm -f "$COOKIES_ADMIN"
resumen
