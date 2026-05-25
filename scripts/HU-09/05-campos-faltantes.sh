#!/bin/bash
# Escenario 5: body incompleto — falta el campo monto
# Esperado: 400 Bad Request con error de validación

source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 5: campos faltantes (sin monto) ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)

http_code=$(curl -s -o "$_BODY_FILE" -w "%{http_code}" -b "$COOKIES" \
  -X POST "${URL_BASE}/transacciones/depositar" \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$ID}")

[ "$http_code" = "400" ] \
  && ok "HTTP 400 Bad Request (monto faltante)" \
  || fail "Esperado 400, obtenido: $http_code"

resumen
