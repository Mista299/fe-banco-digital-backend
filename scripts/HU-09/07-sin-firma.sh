#!/bin/bash
# Escenario 7: cuenta de otro usuario — acceso no autorizado
# Usuario A intenta depositar en la cuenta de Usuario B (que le pertenece a B)
# Esperado: 404 Not Found (no encuentra la cuenta para el usuario A autenticado)

source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 7: cuenta de otro usuario ==="

echo "--- Registrando usuario B (propietario de la cuenta) ---"
registrar_usuario "B7"
USER_B=$USUARIO_REG; PASS_B=$PASS_REG

# Obtener idCuenta de B
COOKIES_B="$(dirname "$0")/cookies_hu09_b.txt"
rm -f "$COOKIES_B"
curl -s -c "$COOKIES_B" -X POST "${URL_BASE}/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USER_B\",\"password\":\"$PASS_B\"}" > /dev/null

DASH_B=$(curl -s -b "$COOKIES_B" "${URL_BASE}/cuentas/dashboard")
if echo "$DASH_B" | jq -e '.cuentas' > /dev/null 2>&1; then
  ID_B=$(echo "$DASH_B" | jq -r '.cuentas[0].idCuenta')
else
  ID_B=$(echo "$DASH_B" | jq -r '.[0].idCuenta')
fi
rm -f "$COOKIES_B"

echo "  idCuenta de B: $ID_B"

echo "--- Login como usuario A (diferente al propietario) ---"
login "bryan" "bryan123"

echo "--- Usuario A intenta depositar en cuenta de B ---"
http_code=$(depositar "$ID_B" 10000)

[ "$http_code" = "403" ] \
  && ok "HTTP 403 Forbidden (cuenta no pertenece al usuario autenticado)" \
  || fail "Esperado 403, obtenido: $http_code"

resumen
