#!/bin/bash
# Escenario 8: monto negativo (< 0.01)
# Esperado: 400 Bad Request

source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 8: monto negativo ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)

http_code=$(depositar "$ID" -1000)

[ "$http_code" = "400" ] \
  && ok "HTTP 400 Bad Request (monto negativo)" \
  || fail "Esperado 400, obtenido: $http_code"

resumen
