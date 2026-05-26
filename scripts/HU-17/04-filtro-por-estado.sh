#!/bin/bash
# HU-17 · Escenario 2 — Filtrado por estado de cuenta
#
# Valida:
#   - ACTIVA devuelve cuentas con titular, numeroCuenta, saldo, estado
#   - Todos los resultados tienen estado=ACTIVA
#   - INACTIVA devuelve la cuenta de Jorge (saldo 0)
#   - Ningún resultado de INACTIVA tiene estado=ACTIVA
#   - Parámetro faltante → 400

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-17 · Escenario 2 — Filtrado por estado de cuenta"
echo "========================================================"

echo "--- Login como bryan (rol ADMIN) ---"
login "bryan" "bryan123" "$COOKIES_ADMIN"

# ── Estado ACTIVA ─────────────────────────────────────────────────────────────
echo ""
echo "--- GET /reportes/saldos/estado?estado=ACTIVA ---"
RESP=$(get_reporte "estado" "estado=ACTIVA" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK (estado=ACTIVA)" \
  || fail "Esperado 200, obtenido: $HTTP"

LEN=$(echo "$BODY" | jq 'length' 2>/dev/null)
[ -n "$LEN" ] && [ "$LEN" -gt 0 ] \
  && ok "Estado ACTIVA devuelve $LEN cuenta(s)" \
  || fail "Estado ACTIVA no devolvió cuentas"

# Campos requeridos en cada elemento
TIENE_CAMPOS=$(echo "$BODY" | jq '
  [.[] | select(
    (.titular   | type) == "string" and
    (.numeroCuenta | type) == "string" and
    (.saldo     | type) == "number" and
    (.estado    | type) == "string"
  )] | length' 2>/dev/null)
[ "$TIENE_CAMPOS" = "$LEN" ] \
  && ok "Todos los registros tienen titular, numeroCuenta, saldo, estado" \
  || fail "Hay registros con campos faltantes ($TIENE_CAMPOS de $LEN completos)"

# Todos deben tener estado ACTIVA
TODOS_ACTIVA=$(echo "$BODY" | jq '[.[] | select(.estado == "ACTIVA")] | length' 2>/dev/null)
[ "$TODOS_ACTIVA" = "$LEN" ] \
  && ok "Todos los resultados tienen estado=ACTIVA ($LEN/$LEN)" \
  || fail "Hay resultados con estado distinto a ACTIVA ($TODOS_ACTIVA de $LEN)"

# ── Estado INACTIVA ───────────────────────────────────────────────────────────
echo ""
echo "--- GET /reportes/saldos/estado?estado=INACTIVA ---"
RESP=$(get_reporte "estado" "estado=INACTIVA" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK (estado=INACTIVA)" \
  || fail "Esperado 200, obtenido: $HTTP"

LEN_I=$(echo "$BODY" | jq 'length' 2>/dev/null)
[ -n "$LEN_I" ] && [ "$LEN_I" -gt 0 ] \
  && ok "Estado INACTIVA devuelve $LEN_I cuenta(s)" \
  || fail "Estado INACTIVA no devolvió cuentas (seed tiene Jorge con cuenta INACTIVA)"

TODOS_INACTIVA=$(echo "$BODY" | jq '[.[] | select(.estado == "INACTIVA")] | length' 2>/dev/null)
[ "$TODOS_INACTIVA" = "$LEN_I" ] \
  && ok "Todos los resultados tienen estado=INACTIVA ($LEN_I/$LEN_I)" \
  || fail "Hay resultados con estado distinto a INACTIVA"

# Saldo de cuenta inactiva debe ser 0 (Jorge)
SALDO_INACTIVA=$(echo "$BODY" | jq '.[0].saldo // -1' 2>/dev/null)
python3 -c "print(float('${SALDO_INACTIVA:-0}') >= 0)" 2>/dev/null | grep -q "True" \
  && ok "Saldo de cuenta INACTIVA es >= 0: $SALDO_INACTIVA" \
  || fail "Saldo de cuenta INACTIVA inválido: $SALDO_INACTIVA"

# ── Parámetro faltante ────────────────────────────────────────────────────────
echo ""
echo "--- GET /reportes/saldos/estado (sin parámetro) ---"
RESP=$(get_reporte "estado" "" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
[ "$HTTP" = "400" ] \
  && ok "Sin parámetro estado → 400 Bad Request" \
  || fail "Esperado 400 sin parámetro estado, obtenido: $HTTP"

rm -f "$COOKIES_ADMIN"
resumen
