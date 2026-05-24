#!/bin/bash
# Escenario 7: parámetros inválidos → 400
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 7: parámetros inválidos ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)

# Mes fuera de rango (13)
http_code=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIES" "${URL_BASE}/extractos/$ID/2026/13")
[ "$http_code" = "400" ] && ok "400 para mes=13" || fail "Esperado 400 para mes=13, obtenido: $http_code"

# Año futuro
http_code2=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIES" "${URL_BASE}/extractos/$ID/2099/1")
[ "$http_code2" = "400" ] && ok "400 para año futuro" || fail "Esperado 400 para año futuro, obtenido: $http_code2"

resumen
