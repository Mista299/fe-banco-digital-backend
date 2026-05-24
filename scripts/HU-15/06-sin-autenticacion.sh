#!/bin/bash
# Escenario 6: sin token / sin autenticación → 401
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 6: sin autenticación ==="

http_code=$(curl -s -o /dev/null -w "%{http_code}" \
  "${URL_BASE}/extractos/1/2026/4")

[ "$http_code" = "401" ] && ok "401 Unauthorized sin token" \
                           || fail "Esperado 401, obtenido: $http_code"

resumen
