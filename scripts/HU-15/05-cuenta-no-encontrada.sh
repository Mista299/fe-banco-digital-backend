#!/bin/bash
# Escenario 5: id de cuenta inexistente → 403 (no pertenece al usuario)
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 5: cuenta inexistente ==="
login "bryan" "bryan123"

http_code=$(curl -s -o /dev/null -w "%{http_code}" \
  -b "$COOKIES" "${URL_BASE}/extractos/999999/2026/4")

[ "$http_code" = "403" ] && ok "403 Forbidden para cuenta inexistente" \
                           || fail "Esperado 403, obtenido: $http_code"

resumen
