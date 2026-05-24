#!/bin/bash
# Escenario 3: mes en curso → 422 con mensaje informativo
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 3: mes en curso ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)

ANIO=$(date +%Y)
MES=$(date +%-m)

resp=$(extracto_json "$ID" "$ANIO" "$MES")
http_code=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIES" "${URL_BASE}/extractos/$ID/$ANIO/$MES")
mensaje=$(echo "$resp" | jq -r '.mensaje // empty')

if [ "$http_code" = "422" ]; then
  expected="El extracto oficial estará disponible al finalizar el periodo actual"
  [ "$mensaje" = "$expected" ] && ok "422 con mensaje correcto" \
                                 || fail "422 pero mensaje incorrecto: $mensaje"
else
  fail "Esperado 422, obtenido: $http_code"
fi

resumen
