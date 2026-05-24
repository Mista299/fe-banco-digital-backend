#!/bin/bash
# Variables y helpers compartidos — HU-15 Extracto Bancario

source "$(dirname "${BASH_SOURCE[0]}")/../config.sh"
URL_BASE="${API_BASE}/api/v1"
COOKIES="$(dirname "${BASH_SOURCE[0]}")/cookies_hu15.txt"

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
    echo "[ERROR] No se pudo obtener idCuenta — login fallido o sin cuentas" >&2
    exit 2
  fi
  echo "$id"
}

# Descarga el extracto y devuelve HTTP code. Guarda el PDF en /tmp si es 200.
# Uso: http_code=$(extracto <idCuenta> <anio> <mes>)
extracto() {
  curl -s -o /tmp/extracto_hu15.pdf -w "%{http_code}" \
    -b "$COOKIES" \
    "${URL_BASE}/extractos/$1/$2/$3"
}

# Descarga capturando body y HTTP code en una sola petición.
# Exporta: BODY_EXTRACTO con el cuerpo; devuelve HTTP code por stdout.
extracto_con_body() {
  local tmp
  tmp=$(mktemp)
  local code
  code=$(curl -s -o "$tmp" -w "%{http_code}" -b "$COOKIES" "${URL_BASE}/extractos/$1/$2/$3")
  BODY_EXTRACTO=$(cat "$tmp")
  rm -f "$tmp"
  echo "$code"
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
