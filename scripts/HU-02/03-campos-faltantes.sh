#!/bin/bash
# HU-02 · Escenario negativo: campos obligatorios faltantes
#
# Valida:
#   - HTTP 400 Bad Request cuando faltan nombre, email o username

source "$(dirname "$0")/../config.sh"
URL_BASE="${API_BASE}/api/v1"
PASS=0
FAIL=0

ok()   { echo "  [PASS] $1"; PASS=$((PASS + 1)); }
fail() { echo "  [FAIL] $1"; FAIL=$((FAIL + 1)); }

post() {
  local label="$1" body="$2"
  local resp code
  resp=$(curl -s -w "\n%{http_code}" -X POST "${URL_BASE}/registro" \
    -H "Content-Type: application/json" -d "$body")
  code=$(echo "$resp" | tail -1)
  echo "  → $label: HTTP $code"
  [ "$code" = "400" ] \
    && ok "$label devuelve 400" \
    || fail "$label: esperado 400, obtenido $code"
}

echo "========================================================"
echo "  HU-02 · Escenario 3 — Campos obligatorios faltantes"
echo "========================================================"

post "Sin nombre" '{
  "documento":"11111111","fechaExpedicion":"2015-01-01",
  "email":"a@b.co","direccion":"Calle 1","telefono":"3001111111",
  "username":"sinnom01","password":"Prueba123"
}'

post "Sin email" '{
  "documento":"22222222","fechaExpedicion":"2015-01-01",
  "nombre":"Sin Email","direccion":"Calle 1","telefono":"3001111111",
  "username":"sinema01","password":"Prueba123"
}'

post "Sin username" '{
  "documento":"33333333","fechaExpedicion":"2015-01-01",
  "nombre":"Sin User","email":"b@b.co","direccion":"Calle 1","telefono":"3001111111",
  "password":"Prueba123"
}'

post "Sin password" '{
  "documento":"44444444","fechaExpedicion":"2015-01-01",
  "nombre":"Sin Pass","email":"c@b.co","direccion":"Calle 1","telefono":"3001111111",
  "username":"sinpas01"
}'

post "Username muy corto (< 4 chars)" '{
  "documento":"55555555","fechaExpedicion":"2015-01-01",
  "nombre":"Short User","email":"d@b.co","direccion":"Calle 1","telefono":"3001111111",
  "username":"ab","password":"Prueba123"
}'

echo ""
echo "========================================================"
printf "  Resultado: %d pasaron · %d fallaron\n" "$PASS" "$FAIL"
echo "========================================================"
[ "$FAIL" -eq 0 ] && exit 0 || exit 1
