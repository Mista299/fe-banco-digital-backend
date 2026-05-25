#!/bin/bash
# Variables y helpers compartidos — HU-10 Retiro autenticado

source "$(dirname "${BASH_SOURCE[0]}")/../config.sh"
URL_BASE="${API_BASE}/api/v1"
COOKIES="$(dirname "${BASH_SOURCE[0]}")/cookies_hu10.txt"

PASS=0; FAIL=0

login() {
  rm -f "$COOKIES"
  curl -s -c "$COOKIES" -X POST "${URL_BASE}/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$1\",\"password\":\"$2\"}" > /dev/null
}

get_id_cuenta() {
  local dash lista id
  dash=$(curl -s -b "$COOKIES" "${URL_BASE}/cuentas/dashboard")
  if echo "$dash" | jq -e '.cuentas' > /dev/null 2>&1; then
    lista=$(echo "$dash" | jq '.cuentas')
  else
    lista="$dash"
  fi
  id=$(echo "$lista" | jq -r '.[0].idCuenta // empty')
  if [ -z "$id" ]; then
    echo "[ERROR] No se pudo obtener idCuenta" >&2
    exit 2
  fi
  echo "$id"
}

get_saldo() {
  local dash lista
  dash=$(curl -s -b "$COOKIES" "${URL_BASE}/cuentas/dashboard")
  if echo "$dash" | jq -e '.cuentas' > /dev/null 2>&1; then
    lista=$(echo "$dash" | jq '.cuentas')
  else
    lista="$dash"
  fi
  echo "$lista" | jq -r '.[0].saldo // empty'
}

_BODY_FILE=$(mktemp)
retirar() {
  curl -s -o "$_BODY_FILE" -w "%{http_code}" -b "$COOKIES" \
    -X POST "${URL_BASE}/transacciones/retirar" \
    -H "Content-Type: application/json" \
    -d "{\"idCuenta\":$1,\"monto\":$2}"
}

registrar_usuario() {
  local suf="$1"
  local ts; ts=$(date +%s%3N)
  local doc="7${ts: -8}" email="hu10${ts}${suf}@nexus.co" user="hu10${ts}${suf}"
  local resp
  resp=$(curl -s -X POST "${URL_BASE}/registro" \
    -H "Content-Type: application/json" \
    -d "{
      \"documento\":\"$doc\",
      \"genero\":\"FEMENINO\",
      \"fechaExpedicion\":\"2015-01-15\",
      \"nombre\":\"Test HU10 ${suf}\",
      \"email\":\"$email\",
      \"direccion\":\"Calle 1 #2-3\",
      \"telefono\":\"300${ts: -7}\",
      \"username\":\"$user\",
      \"password\":\"Pass1234\"
    }")
  USUARIO_REG=$user
  PASS_REG="Pass1234"
  echo "$resp"
}

ok()   { echo "  [PASS] $1"; PASS=$((PASS + 1)); }
fail() { echo "  [FAIL] $1"; FAIL=$((FAIL + 1)); }

resumen() {
  echo ""
  echo "========================================================"
  printf "  Resultado HU-10: %d pasaron · %d fallaron\n" "$PASS" "$FAIL"
  echo "========================================================"
  [ "$FAIL" -eq 0 ] && exit 0 || exit 1
}
