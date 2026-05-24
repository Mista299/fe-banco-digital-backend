#!/bin/bash
# Escenario 1: mes anterior (cerrado, puede tener movimientos seed) → 200 + PDF válido
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 1: mes cerrado con movimientos ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)

# Mes anterior siempre está cerrado; si el seed tiene datos este mes, están aquí.
ANIO_ANT=$(date +%Y)
MES_ANT=$(date +%m | sed 's/^0//')
if [ "$MES_ANT" -eq 1 ]; then
  ANIO_ANT=$((ANIO_ANT - 1))
  MES_ANT=12
else
  MES_ANT=$((MES_ANT - 1))
fi

http_code=$(extracto "$ID" "$ANIO_ANT" "$MES_ANT")

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
