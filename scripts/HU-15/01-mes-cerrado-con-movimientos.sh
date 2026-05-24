#!/bin/bash
# Escenario 1: mes cerrado (abril 2026) con movimientos → 200 + PDF válido
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 1: mes cerrado con movimientos ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)

http_code=$(extracto "$ID" 2026 4)

if [ "$http_code" = "200" ]; then
  header=$(head -c 4 /tmp/extracto_hu15.pdf)
  if [ "$header" = "%PDF" ]; then
    ok "200 OK y el archivo es un PDF válido"
  else
    fail "200 pero el archivo no empieza con %PDF"
  fi
else
  fail "Esperado 200, obtenido: $http_code"
fi

resumen
