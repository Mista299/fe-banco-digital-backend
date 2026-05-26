#!/bin/bash
# HU-18 · Seguridad — Rol CLIENTE no puede acceder
#
# Valida:
#   - HTTP 403 Forbidden cuando un usuario con rol CLIENTE
#     intenta consultar actividad de otro cliente

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-18 · Seguridad 2 — Acceso denegado (rol CLIENTE)"
echo "========================================================"

echo "--- Login como ana (rol CLIENTE) ---"
login "ana" "ana123" "$COOKIES_CLIENTE"

echo ""
echo "--- GET /buscar/documento con rol CLIENTE ---"
RESP=$(buscar_doc "$DOC_ANA" "" "" "" "$COOKIES_CLIENTE")
HTTP=$(echo "$RESP" | tail -1)
echo "  HTTP: $HTTP"

[ "$HTTP" = "403" ] \
  && ok "GET /buscar/documento con CLIENTE → 403 Forbidden" \
  || fail "Esperado 403, obtenido: $HTTP"

echo ""
echo "--- GET /buscar/cuenta con rol CLIENTE ---"
RESP=$(buscar_cuenta "$CTA_ANA" "" "" "" "$COOKIES_CLIENTE")
HTTP=$(echo "$RESP" | tail -1)
echo "  HTTP: $HTTP"

[ "$HTTP" = "403" ] \
  && ok "GET /buscar/cuenta con CLIENTE → 403 Forbidden" \
  || fail "Esperado 403, obtenido: $HTTP"

rm -f "$COOKIES_CLIENTE"
resumen
