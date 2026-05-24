#!/bin/bash
# Escenario 3: mes en curso → 422 con mensaje informativo
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 3: mes en curso ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)

ANIO=$(date +%Y)
MES=$(date +%m | sed 's/^0//')

http_code=$(extracto_con_body "$ID" "$ANIO" "$MES")
mensaje=$(jq -r '.mensaje // empty' "$_BODY_FILE")

if [ "$http_code" = "422" ]; then
  expected="El extracto oficial estará disponible al finalizar el periodo actual"
  [ "$mensaje" = "$expected" ] && ok "422 con mensaje correcto" \
                                 || fail "422 pero mensaje incorrecto: $mensaje"
else
  fail "Esperado 422, obtenido: $http_code"
fi

resumen
