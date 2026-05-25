#!/bin/bash
# HU-02 · Escenario 1: Registro exitoso con apertura automática de cuenta de ahorros
#
# Valida:
#   - HTTP 201 Created
#   - numeroCuenta de 8 dígitos presente en la respuesta
#   - tipo cuenta = AHORROS (verificado vía dashboard post-login)
#   - saldo inicial = 0
#   - mensaje de confirmación presente

source "$(dirname "$0")/../config.sh"
URL_BASE="${API_BASE}/api/v1"
COOKIES="$(dirname "$0")/cookies_hu02.txt"
PASS=0
FAIL=0

ok()   { echo "  [PASS] $1"; PASS=$((PASS + 1)); }
fail() { echo "  [FAIL] $1"; FAIL=$((FAIL + 1)); }

# ─── Datos únicos por timestamp ───────────────────────────────────────────────
TS=$(date +%s)
DOC="9${TS: -8}"
EMAIL="test${TS}@nexus.co"
USER="usr${TS}"

BODY=$(cat <<EOF
{
  "documento": "$DOC",
  "genero": "MASCULINO",
  "fechaExpedicion": "2015-06-20",
  "nombre": "Usuario Prueba HU02",
  "email": "$EMAIL",
  "direccion": "Cra 50 #30-10, Medellin",
  "telefono": "3${TS: -9}",
  "username": "$USER",
  "password": "Prueba123"
}
EOF
)

echo "========================================================"
echo "  HU-02 · Escenario 1 — Registro exitoso"
echo "========================================================"
echo "  documento : $DOC"
echo "  username  : $USER"
echo "  email     : $EMAIL"
echo ""

# ─── POST /registro ──────────────────────────────────────────────────────────
echo "--- POST /api/v1/registro ---"
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${URL_BASE}/registro" \
  -H "Content-Type: application/json" \
  -d "$BODY")

HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY_RESP=$(echo "$RESPONSE" | head -n -1)

echo "$BODY_RESP" | jq . 2>/dev/null || echo "$BODY_RESP"
echo ""

# ─── Validaciones Escenario 1 ─────────────────────────────────────────────────
echo "--- Validaciones Escenario 1 ---"

[ "$HTTP_CODE" = "201" ] && ok "HTTP 201 Created" || fail "HTTP esperado 201, obtenido $HTTP_CODE"

NUMERO=$(echo "$BODY_RESP" | jq -r '.numeroCuenta // empty')
[ -n "$NUMERO" ] && ok "numeroCuenta presente: $NUMERO" || fail "numeroCuenta ausente en la respuesta"

LONGITUD=${#NUMERO}
[ "$LONGITUD" -ge 8 ] && ok "numeroCuenta con $LONGITUD dígitos ($NUMERO)" \
                       || fail "numeroCuenta demasiado corto ($LONGITUD dígitos): $NUMERO"

SALDO=$(echo "$BODY_RESP" | jq -r '.saldo // empty')
{ [ "$SALDO" = "0" ] || [ "$SALDO" = "0.0000" ] || [ "$SALDO" = "0.00" ]; } \
  && ok "Saldo inicial = \$0 COP" || fail "Saldo inicial esperado 0, obtenido '$SALDO'"

MENSAJE=$(echo "$BODY_RESP" | jq -r '.mensaje // empty')
[ -n "$MENSAJE" ] && ok "mensaje de confirmación presente" || fail "mensaje ausente en la respuesta"

# ─── Escenario 2: verificar cuenta visible en dashboard ──────────────────────
echo ""
echo "========================================================"
echo "  HU-02 · Escenario 2 — Dashboard post-registro"
echo "========================================================"

rm -f "$COOKIES"
curl -s -c "$COOKIES" -X POST "${URL_BASE}/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USER\",\"password\":\"Prueba123\"}" | jq . 2>/dev/null
echo ""

echo "--- GET /api/v1/cuentas/dashboard ---"
DASH=$(curl -s -b "$COOKIES" "${URL_BASE}/cuentas/dashboard")
echo "$DASH" | jq . 2>/dev/null || echo "$DASH"
echo ""

# Dashboard devuelve array directo de cuentas
CUENTA_COUNT=$(echo "$DASH" | jq 'if type=="array" then length else 0 end')
echo "--- Validaciones Escenario 2 ---"

[ "$CUENTA_COUNT" -ge 1 ] \
  && ok "Dashboard muestra $CUENTA_COUNT cuenta(s)" \
  || fail "Dashboard no retornó ninguna cuenta"

CUENTA=$(echo "$DASH" | jq --arg num "$NUMERO" \
  'if type=="array" then .[] | select(.numeroCuenta == $num) else empty end')
[ -n "$CUENTA" ] && ok "Cuenta $NUMERO visible en el dashboard" \
                 || fail "Cuenta $NUMERO no aparece en el dashboard"

TIPO=$(echo "$CUENTA" | jq -r '.tipo // empty')
[ "$TIPO" = "AHORROS" ] && ok "Tipo de cuenta = AHORROS" \
                         || fail "Tipo esperado AHORROS, obtenido '$TIPO'"

ESTADO_DASH=$(echo "$CUENTA" | jq -r '.estado // empty')
[ "$ESTADO_DASH" = "ACTIVA" ] && ok "Estado en dashboard = ACTIVA" \
                               || fail "Estado esperado ACTIVA, obtenido '$ESTADO_DASH'"

SALDO_DASH=$(echo "$CUENTA" | jq -r '.saldo // empty')
{ [ "$SALDO_DASH" = "0" ] || [ "$SALDO_DASH" = "0.0000" ] || [ "$SALDO_DASH" = "0.00" ]; } \
  && ok "Saldo en dashboard = \$0 COP" || fail "Saldo esperado 0, obtenido '$SALDO_DASH'"

# ─── Resumen ──────────────────────────────────────────────────────────────────
echo ""
echo "========================================================"
printf "  Resultado: %d pasaron · %d fallaron\n" "$PASS" "$FAIL"
echo "========================================================"
[ "$FAIL" -eq 0 ] && exit 0 || exit 1
