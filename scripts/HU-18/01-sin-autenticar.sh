#!/bin/bash
# HU-18 · Seguridad — Sin autenticar
#
# Valida:
#   - HTTP 401 al consultar /buscar/documento sin sesión activa
#   - HTTP 401 al consultar /buscar/cuenta sin sesión activa

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-18 · Seguridad 1 — Sin autenticar"
echo "========================================================"

echo "--- GET /buscar/documento sin cookies ---"
RESP=$(buscar_doc_sin_auth "$DOC_ANA")
HTTP=$(echo "$RESP" | tail -1)
echo "  HTTP: $HTTP"

[ "$HTTP" = "401" ] \
  && ok "GET /buscar/documento sin sesión → 401 Unauthorized" \
  || fail "Esperado 401, obtenido: $HTTP"

echo ""
echo "--- GET /buscar/cuenta sin cookies ---"
RESP=$(buscar_cuenta_sin_auth "$CTA_ANA")
HTTP=$(echo "$RESP" | tail -1)
echo "  HTTP: $HTTP"

[ "$HTTP" = "401" ] \
  && ok "GET /buscar/cuenta sin sesión → 401 Unauthorized" \
  || fail "Esperado 401, obtenido: $HTTP"

resumen
