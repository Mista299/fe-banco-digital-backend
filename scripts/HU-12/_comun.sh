#!/bin/bash
# Variables y helpers compartidos — HU-12 Transferencia Interbancaria ACH

source "$(dirname "${BASH_SOURCE[0]}")/../config.sh"
URL_BASE="${API_BASE}/api/v1"
TRANSFER_URL="${URL_BASE}/transferencias/interbancarias"
COOKIES="$(dirname "${BASH_SOURCE[0]}")/cookies_hu12.txt"
GATEWAY_SECRET="clave_secreta_pasarela_banco_2026_hmac"

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

# Crea una transferencia interbancaria. Retorna el body JSON.
crear_transferencia() {
  local id_origen="$1" monto="${2:-50000}"
  curl -s -b "$COOKIES" \
    -X POST "$TRANSFER_URL" \
    -H "Content-Type: application/json" \
    -d "{
      \"idCuentaOrigen\": $id_origen,
      \"bancoDestino\": \"BANCO_EXTERNAL_01\",
      \"numeroCuentaDestino\": \"999888777\",
      \"tipoCuentaDestino\": \"AHORROS\",
      \"tipoDocumentoReceptor\": \"CC\",
      \"numeroDocumentoReceptor\": \"1234567890\",
      \"nombreReceptor\": \"Juan Perez\",
      \"monto\": $monto
    }"
}

ok()   { echo "  [PASS] $1"; PASS=$((PASS + 1)); }
fail() { echo "  [FAIL] $1"; FAIL=$((FAIL + 1)); }

resumen() {
  echo ""
  echo "========================================================"
  printf "  Resultado HU-12: %d pasaron · %d fallaron\n" "$PASS" "$FAIL"
  echo "========================================================"
  [ "$FAIL" -eq 0 ] && exit 0 || exit 1
}
