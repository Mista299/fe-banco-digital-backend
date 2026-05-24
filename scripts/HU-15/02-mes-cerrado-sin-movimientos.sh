#!/bin/bash
# Escenario 2: mes cerrado sin movimientos → 200 + PDF (saldo inicial = saldo final)
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 2: mes cerrado sin movimientos ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)

# Enero 2020 garantizadamente no tiene movimientos seed
http_code=$(extracto "$ID" 2020 1)

if [ "$http_code" = "200" ]; then
  header=$(head -c 4 /tmp/extracto_hu15.pdf)
  [ "$header" = "%PDF" ] && ok "200 OK PDF válido (periodo sin movimientos)" \
                           || fail "200 pero el archivo no empieza con %PDF"
else
  fail "Esperado 200, obtenido: $http_code"
fi

resumen
