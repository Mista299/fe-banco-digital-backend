#!/bin/bash
# Escenario 3: depósito sin sesión activa
# Esperado: 401 Unauthorized (no hay cookies de autenticación)

source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 3: sin autenticación ==="

# No hacemos login — depositamos directamente sin cookies
http_code=$(curl -s -o "$_BODY_FILE" -w "%{http_code}" \
  -X POST "${URL_BASE}/transacciones/depositar" \
  -H "Content-Type: application/json" \
  -d '{"idCuenta":1,"monto":50000}')

[ "$http_code" = "401" ] \
  && ok "HTTP 401 Unauthorized (sin sesión)" \
  || fail "Esperado 401, obtenido: $http_code"

resumen
