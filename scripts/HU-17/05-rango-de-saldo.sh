#!/bin/bash
# HU-17 · Escenario 3 — Filtrado por rango de saldo
#
# Valida:
#   - min solo: devuelve cuentas con saldo > min
#   - min+max: devuelve cuentas dentro del rango
#   - Todos los saldos en respuesta cumplen el criterio
#   - Rango sin resultados (min muy alto) devuelve lista vacía
#   - Sin parámetro min → 400

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-17 · Escenario 3 — Filtrado por rango de saldo"
echo "========================================================"

echo "--- Login como bryan (rol ADMIN) ---"
login "bryan" "bryan123" "$COOKIES_ADMIN"

# ── Solo min ──────────────────────────────────────────────────────────────────
echo ""
echo "--- GET /reportes/saldos/rango?min=500000 ---"
RESP=$(get_reporte "rango" "min=500000" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(unwrap_list "$(echo "$RESP" | head -n -1)")

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK (solo min=500000)" \
  || fail "Esperado 200, obtenido: $HTTP"

LEN=$(echo "$BODY" | jq 'length' 2>/dev/null)
[ -n "$LEN" ] && [ "$LEN" -gt 0 ] \
  && ok "Devuelve $LEN cuenta(s) con saldo > 500000" \
  || fail "Ninguna cuenta con saldo > 500000 (seed tiene varias)"

# Campos requeridos: idCuenta, saldoDisponible, saldoContable, estado, tipoCuenta
TIENE_CAMPOS=$(echo "$BODY" | jq '
  [.[] | select(
    (.idCuenta       | type) == "number" and
    (.saldoDisponible | type) == "number" and
    (.saldoContable  | type) == "number" and
    (.estado         | type) == "string" and
    (.tipoCuenta     | type) == "string"
  )] | length' 2>/dev/null)
[ "$TIENE_CAMPOS" = "$LEN" ] \
  && ok "Todos los registros tienen idCuenta, saldoDisponible, saldoContable, estado, tipoCuenta" \
  || fail "Registros con campos incompletos ($TIENE_CAMPOS de $LEN completos)"

# Todos los saldos deben ser > 500000
FUERA=$(echo "$BODY" | jq '[.[] | select(.saldoDisponible <= 500000)] | length' 2>/dev/null)
[ "$FUERA" = "0" ] \
  && ok "Todos los saldos devueltos son > 500000" \
  || fail "$FUERA cuenta(s) con saldo <= 500000 en el resultado"

# ── min + max ─────────────────────────────────────────────────────────────────
echo ""
echo "--- GET /reportes/saldos/rango?min=500000&max=800000 ---"
RESP=$(get_reporte "rango" "min=500000&max=800000" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(unwrap_list "$(echo "$RESP" | head -n -1)")

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK (min=500000 max=800000)" \
  || fail "Esperado 200, obtenido: $HTTP"

LEN_R=$(echo "$BODY" | jq 'length' 2>/dev/null)
[ -n "$LEN_R" ] && [ "$LEN_R" -gt 0 ] \
  && ok "Devuelve $LEN_R cuenta(s) en rango [500000, 800000]" \
  || fail "Ninguna cuenta en rango [500000, 800000]"

# Todos dentro del rango
FUERA_R=$(echo "$BODY" | jq '
  [.[] | select(.saldoDisponible < 500000 or .saldoDisponible > 800000)] | length' 2>/dev/null)
[ "$FUERA_R" = "0" ] \
  && ok "Todos los saldos están dentro del rango [500000, 800000]" \
  || fail "$FUERA_R cuenta(s) fuera del rango en el resultado"

# El filtrado con max debe traer menos o igual que solo min
MENOS=$(python3 -c "print($LEN_R <= $LEN)" 2>/dev/null)
[ "$MENOS" = "True" ] \
  && ok "Rango [min,max] ($LEN_R) ≤ solo min ($LEN): filtrado coherente" \
  || fail "Rango [min,max] ($LEN_R) > solo min ($LEN): incoherente"

# ── Rango sin resultados ──────────────────────────────────────────────────────
echo ""
echo "--- GET /reportes/saldos/rango?min=99999999 (sin resultados) ---"
RESP=$(get_reporte "rango" "min=99999999" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(unwrap_list "$(echo "$RESP" | head -n -1)")

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK (min muy alto)" \
  || fail "Esperado 200, obtenido: $HTTP"
LEN_EMPTY=$(echo "$BODY" | jq 'length' 2>/dev/null)
[ "$LEN_EMPTY" = "0" ] \
  && ok "min=99999999 devuelve lista vacía (0 cuentas)" \
  || fail "Esperado 0 cuentas, obtenido: $LEN_EMPTY"

# ── Sin parámetro min ─────────────────────────────────────────────────────────
echo ""
echo "--- GET /reportes/saldos/rango (sin parámetro) ---"
RESP=$(get_reporte "rango" "" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
[ "$HTTP" = "400" ] \
  && ok "Sin parámetro min → 400 Bad Request" \
  || fail "Esperado 400 sin parámetro min, obtenido: $HTTP"

rm -f "$COOKIES_ADMIN"
resumen
