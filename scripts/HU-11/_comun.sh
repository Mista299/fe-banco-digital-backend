#!/bin/bash
# Variables y funciones compartidas — source este archivo desde cada script

source "$(dirname "${BASH_SOURCE[0]}")/../config.sh"
URL_BASE="${API_BASE}/api/v1"

# Registra un usuario nuevo con datos únicos basados en timestamp
# Uso: registrar_usuario <sufijo>
# Retorna: imprime el body de respuesta y exporta NUMERO_CUENTA_REG, ID_CUENTA_REG
registrar_usuario() {
  local suf="$1"
  local ts; ts=$(date +%s%3N)
  local doc="7${ts: -8}" email="t${ts}${suf}@nexus.co" user="usr${ts}${suf}"

  local resp
  resp=$(curl -s -X POST "${URL_BASE}/registro" \
    -H "Content-Type: application/json" \
    -d "{
      \"documento\":\"$doc\",
      \"genero\":\"MASCULINO\",
      \"fechaExpedicion\":\"2015-01-15\",
      \"nombre\":\"Test ${suf}\",
      \"email\":\"$email\",
      \"direccion\":\"Calle 1 #2-3\",
      \"telefono\":\"300${ts: -7}\",
      \"username\":\"$user\",
      \"password\":\"Pass1234\"
    }")

  echo "$resp"
  NUMERO_CUENTA_REG=$(echo "$resp" | jq -r '.numeroCuenta // empty')
  USUARIO_REG=$user
  PASS_REG="Pass1234"
}

# Inicia sesión y guarda cookies en el archivo dado
# Uso: login <usuario> <password> <archivo_cookies>
login() {
  curl -s -c "$3" -X POST "${URL_BASE}/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$1\",\"password\":\"$2\"}" > /dev/null
}

# Obtiene idCuenta y numeroCuenta de la primera cuenta activa del dashboard.
# Soporta dos formatos:
#   - { mensajeBienvenida, cuentas: [...] }   (DashboardResponseDTO)
#   - [ {...}, {...} ]                         (List<CuentaResumenDTO> legacy)
# Uso: get_cuenta <archivo_cookies>
# Exporta: ID_CUENTA_DASH, NUM_CUENTA_DASH
get_cuenta() {
  local dash lista
  dash=$(curl -s -b "$1" "${URL_BASE}/cuentas/dashboard")
  if echo "$dash" | jq -e '.cuentas' > /dev/null 2>&1; then
    lista=$(echo "$dash" | jq '.cuentas')
  else
    lista="$dash"
  fi
  ID_CUENTA_DASH=$(echo "$lista" | jq -r '.[0].idCuenta // empty')
  NUM_CUENTA_DASH=$(echo "$lista" | jq -r '.[0].numeroCuenta // empty')
}

# Deposita monto en la cuenta del usuario autenticado por cookies
# Uso: depositar <archivo_cookies> <idCuenta> <monto>
depositar() {
  curl -s -b "$1" -X POST "${URL_BASE}/transacciones/depositar" \
    -H "Content-Type: application/json" \
    -d "{\"idCuenta\":$2,\"monto\":$3}"
}

# Ejecuta una transferencia y retorna body + http_code separados por newline
# Uso: transferir <cookies> <idOrigen> <numOrigen> <numDestino> <monto>
transferir() {
  curl -s -w "\n%{http_code}" -b "$1" \
    -X POST "${URL_BASE}/transacciones/transferir" \
    -H "Content-Type: application/json" \
    -d "{\"idCuentaOrigen\":$2,\"numeroCuentaDestino\":\"$4\",\"monto\":$5}"
}

# Devuelve el saldo de la primera cuenta del usuario autenticado
# Uso: saldo=$(get_saldo <archivo_cookies>)
get_saldo() {
  local dash lista
  dash=$(curl -s -b "$1" "${URL_BASE}/cuentas/dashboard")
  if echo "$dash" | jq -e '.cuentas' > /dev/null 2>&1; then
    lista=$(echo "$dash" | jq '.cuentas')
  else
    lista="$dash"
  fi
  echo "$lista" | jq -r '.[0].saldo // empty'
}

ok()   { echo "  [PASS] $1"; PASS=$((PASS + 1)); }
fail() { echo "  [FAIL] $1"; FAIL=$((FAIL + 1)); }

resumen() {
  echo ""
  echo "========================================================"
  printf "  Resultado: %d pasaron · %d fallaron\n" "$PASS" "$FAIL"
  echo "========================================================"
  [ "$FAIL" -eq 0 ] && exit 0 || exit 1
}
