#!/bin/bash
# Escenario 4: intento de depósito en cuenta BLOQUEADA
# Se registra un usuario nuevo, se bloquea su cuenta, se intenta depositar.
# Esperado: 400 Bad Request

source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 4: cuenta bloqueada ==="

echo "--- Registrando usuario temporal ---"
registrar_usuario "blq"
login "$USUARIO_REG" "$PASS_REG"
ID=$(get_id_cuenta)

echo "--- Bloqueando cuenta ---"
curl -s -b "$COOKIES" -X POST "${URL_BASE}/cuentas/seguridad/bloquear" \
  -H "Content-Type: application/json" \
  -d "{\"password\":\"$PASS_REG\"}" > /dev/null

echo "--- Intentando depósito en cuenta bloqueada ---"
http_code=$(depositar "$ID" 10000)
mensaje=$(jq -r '.mensaje // empty' "$_BODY_FILE")

[ "$http_code" = "400" ] \
  && ok "HTTP 400 Bad Request (cuenta bloqueada)" \
  || fail "Esperado 400, obtenido: $http_code — $mensaje"

resumen
