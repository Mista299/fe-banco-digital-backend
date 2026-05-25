#!/bin/bash
# Escenario 2: saldo insuficiente
# Esperado: 409 Conflict

source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 2: saldo insuficiente ==="

registrar_usuario "R2"
login "$USUARIO_REG" "$PASS_REG"
ID=$(get_id_cuenta)

# Cuenta con saldo 0 — intentar retirar 100
http_code=$(retirar "$ID" 100)
mensaje=$(jq -r '.mensaje // empty' "$_BODY_FILE")

[ "$http_code" = "409" ] \
  && ok "HTTP 409 Conflict (saldo insuficiente)" \
  || fail "Esperado 409, obtenido: $http_code — $mensaje"

resumen
