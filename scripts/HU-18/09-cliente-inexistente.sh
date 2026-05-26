#!/bin/bash
# HU-18 · Manejo de errores — Identificador inexistente
#
# Valida:
#   - HTTP 4xx al buscar un documento que no existe en la BD
#   - HTTP 4xx al buscar un número de cuenta que no existe
#   - La respuesta de error incluye un mensaje legible

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-18 · Errores — Identificador inexistente"
echo "========================================================"

echo "--- Login como bryan (rol ADMIN) ---"
login "bryan" "bryan123" "$COOKIES_ADMIN"

# ── Documento inexistente ─────────────────────────────────────────────────────
echo ""
echo "--- GET /buscar/documento?documento=00000000 (no existe) ---"
RESP=$(buscar_doc "00000000" "" "" "" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)
echo "  HTTP: $HTTP"
echo "  Mensaje: $(echo "$BODY" | jq -r '.mensaje // .' 2>/dev/null)"

[[ "$HTTP" =~ ^4 ]] \
  && ok "Documento inexistente → HTTP $HTTP (error esperado)" \
  || fail "Esperado HTTP 4xx para documento inexistente, obtenido: $HTTP"

MENSAJE=$(echo "$BODY" | jq -r '.mensaje // empty')
[ -n "$MENSAJE" ] \
  && ok "Respuesta incluye mensaje de error: $MENSAJE" \
  || fail "Respuesta de error sin campo 'mensaje'"

# ── Cuenta inexistente ────────────────────────────────────────────────────────
echo ""
echo "--- GET /buscar/cuenta?numeroCuenta=99999999 (no existe) ---"
RESP=$(buscar_cuenta "99999999" "" "" "" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)
echo "  HTTP: $HTTP"
echo "  Mensaje: $(echo "$BODY" | jq -r '.mensaje // .' 2>/dev/null)"

[[ "$HTTP" =~ ^4 ]] \
  && ok "Cuenta inexistente → HTTP $HTTP (error esperado)" \
  || fail "Esperado HTTP 4xx para cuenta inexistente, obtenido: $HTTP"

MENSAJE=$(echo "$BODY" | jq -r '.mensaje // empty')
[ -n "$MENSAJE" ] \
  && ok "Respuesta incluye mensaje de error: $MENSAJE" \
  || fail "Respuesta de error sin campo 'mensaje'"

rm -f "$COOKIES_ADMIN"
resumen
