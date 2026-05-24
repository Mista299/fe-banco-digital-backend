#!/bin/bash
# Variables y helpers compartidos — HU-15 Extracto Bancario

source "$(dirname "${BASH_SOURCE[0]}")/../config.sh"
URL_BASE="${API_BASE}/api/v1"
COOKIES="$(dirname "${BASH_SOURCE[0]}")/cookies_hu15.txt"

PASS=0; FAIL=0

login() {
  curl -s -c "$COOKIES" -X POST "${URL_BASE}/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$1\",\"password\":\"$2\"}" > /dev/null
}

get_id_cuenta() {
  local dash lista
  dash=$(curl -s -b "$COOKIES" "${URL_BASE}/cuentas/dashboard")
  if echo "$dash" | jq -e '.cuentas' > /dev/null 2>&1; then
    lista=$(echo "$dash" | jq '.cuentas')
  else
    lista="$dash"
  fi
  echo "$lista" | jq -r '.[0].idCuenta // empty'
}

# Descarga el extracto y devuelve HTTP code. Guarda el PDF en /tmp si es 200.
# Uso: http_code=$(extracto <idCuenta> <anio> <mes>)
extracto() {
  curl -s -o /tmp/extracto_hu15.pdf -w "%{http_code}" \
    -b "$COOKIES" \
    "${URL_BASE}/extractos/$1/$2/$3"
}

# Descarga con output JSON (para errores)
extracto_json() {
  curl -s -b "$COOKIES" "${URL_BASE}/extractos/$1/$2/$3"
}

ok()   { echo "  [PASS] $1"; PASS=$((PASS + 1)); }
fail() { echo "  [FAIL] $1"; FAIL=$((FAIL + 1)); }

resumen() {
  echo ""
  echo "========================================================"
  printf "  Resultado HU-15: %d pasaron · %d fallaron\n" "$PASS" "$FAIL"
  echo "========================================================"
  [ "$FAIL" -eq 0 ] && exit 0 || exit 1
}
