#!/bin/bash
# Escenario 4: monto inválido (= 0)
# Esperado: 400 Bad Request

source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 4: monto inválido (= 0) ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)

http_code=$(retirar "$ID" 0)

[ "$http_code" = "400" ] \
  && ok "HTTP 400 Bad Request (monto = 0)" \
  || fail "Esperado 400, obtenido: $http_code"

resumen
