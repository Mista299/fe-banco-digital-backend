#!/bin/bash
# HU-02 · Escenario negativo: documento ya registrado
#
# Valida:
#   - HTTP 409 Conflict al intentar registrar un documento que ya existe

source "$(dirname "$0")/../config.sh"
URL_BASE="${API_BASE}/api/v1"
PASS=0
FAIL=0

ok()   { echo "  [PASS] $1"; PASS=$((PASS + 1)); }
fail() { echo "  [FAIL] $1"; FAIL=$((FAIL + 1)); }

TS=$(date +%s)
DOC="9${TS: -8}"
EMAIL1="orig${TS}@nexus.co"
EMAIL2="dup${TS}@nexus.co"
USER1="orig${TS}"
USER2="dup${TS}"

registro() {
  local doc="$1" email="$2" user="$3"
  curl -s -w "\n%{http_code}" -X POST "${URL_BASE}/registro" \
    -H "Content-Type: application/json" \
    -d "{
      \"documento\":\"$doc\",
      \"genero\":\"FEMENINO\",
      \"fechaExpedicion\":\"2015-06-20\",
      \"nombre\":\"Usuario Test\",
      \"email\":\"$email\",
      \"direccion\":\"Calle 1 #2-3\",
      \"telefono\":\"3001234567\",
      \"username\":\"$user\",
      \"password\":\"Prueba123\"
    }"
}

echo "========================================================"
echo "  HU-02 · Escenario 2 — Documento duplicado"
echo "========================================================"

echo "--- Primer registro (documento: $DOC) ---"
R1=$(registro "$DOC" "$EMAIL1" "$USER1")
CODE1=$(echo "$R1" | tail -1)
echo "$R1" | head -n -1 | jq . 2>/dev/null
echo "HTTP: $CODE1"
echo ""

[ "$CODE1" = "201" ] \
  && ok "Primer registro: HTTP 201" \
  || fail "Primer registro: esperado 201, obtenido $CODE1"

echo "--- Segundo registro con mismo documento ---"
R2=$(registro "$DOC" "$EMAIL2" "$USER2")
CODE2=$(echo "$R2" | tail -1)
echo "$R2" | head -n -1 | jq . 2>/dev/null
echo "HTTP: $CODE2"
echo ""

[ "$CODE2" = "409" ] \
  && ok "Documento duplicado rechazado: HTTP 409" \
  || fail "Documento duplicado: esperado 409, obtenido $CODE2"

echo ""
echo "========================================================"
printf "  Resultado: %d pasaron · %d fallaron\n" "$PASS" "$FAIL"
echo "========================================================"
[ "$FAIL" -eq 0 ] && exit 0 || exit 1
