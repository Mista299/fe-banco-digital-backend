#!/bin/bash
# ============================================================
# Agrega cuentas adicionales a Bryan y genera todos los tipos
# de movimientos en cada una.
#
# Uso: ./scripts/test-bryan-cuentas.sh
# Requiere servidor corriendo con --profiles=seed
# ============================================================

BASE="http://localhost:8080"
COOKIE="/tmp/cookies_bryan_multi.txt"
COOKIE_ANA="/tmp/cookies_ana_multi.txt"
GATEWAY_SECRET="clave_secreta_pasarela_banco_2026_hmac"

PASS=0
FAIL=0

color_ok()   { echo -e "\033[0;32m  OK\033[0m  $1"; }
color_fail() { echo -e "\033[0;31m  KO\033[0m  $1"; }
section()    { echo -e "\n\033[1;36m▶ $1\033[0m"; }
subsection() { echo -e "\033[0;33m  ── $1\033[0m"; }

assert() {
  local label="$1" expected="$2" actual="$3" body="$4"
  if [ "$actual" = "$expected" ]; then
    color_ok "[$actual] $label"
    ((PASS++))
  else
    color_fail "[$actual] $label  (esperado $expected)"
    [ -n "$body" ] && echo "        body: $(echo "$body" | head -c 300)"
    ((FAIL++))
  fi
}

assert_contains() {
  local label="$1" needle="$2" haystack="$3" status="$4"
  if echo "$haystack" | grep -qF "$needle"; then
    color_ok "[$status] $label"
    ((PASS++))
  else
    color_fail "[$status] $label  (no contiene: $needle)"
    echo "        body: $(echo "$haystack" | head -c 300)"
    ((FAIL++))
  fi
}

http() {
  local jar_r="$1"; shift
  local jar_w="$1"; shift
  local method="$1"; shift
  local path="$1"; shift
  local cmd=(curl -s -o /tmp/rb.txt -w "%{http_code}" -X "$method" "${BASE}${path}" "$@")
  [ -n "$jar_r" ] && cmd+=(-b "$jar_r")
  [ -n "$jar_w" ] && cmd+=(-c "$jar_w")
  RESP_CODE=$("${cmd[@]}")
  RESP_BODY=$(cat /tmp/rb.txt)
}

# ─────────────────────────────────────────────────────────────
section "SETUP — Login"
# ─────────────────────────────────────────────────────────────

http "" "$COOKIE" POST /api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"bryan","password":"bryan123"}'
assert "Login bryan" "200" "$RESP_CODE" "$RESP_BODY"

http "" "$COOKIE_ANA" POST /api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ana","password":"ana123"}'
assert "Login ana (para transferencias cruzadas)" "200" "$RESP_CODE" "$RESP_BODY"

if [ "$RESP_CODE" != "200" ] && [ "$(cat /tmp/rb.txt | grep -c bryan)" -eq 0 ]; then
  echo "Login falló — ¿está el servidor corriendo con --profiles=seed?"
  exit 1
fi

# Cuenta original de bryan
http "$COOKIE" "" GET /api/v1/cuentas/dashboard
CTA1_ID=$(echo "$RESP_BODY"   | grep -o '"idCuenta":[0-9]*' | head -1 | cut -d: -f2)
CTA1_NUM=$(echo "$RESP_BODY"  | grep -o '"numeroCuenta":"[^"]*"' | head -1 | cut -d'"' -f4)

# Cuenta de ana (para destino en transferencias)
http "$COOKIE_ANA" "" GET /api/v1/cuentas/dashboard
ANA_NUM=$(echo "$RESP_BODY" | grep -o '"numeroCuenta":"[^"]*"' | head -1 | cut -d'"' -f4)

echo "  Cuenta original Bryan: id=$CTA1_ID  num=$CTA1_NUM"
echo "  Cuenta Ana:            num=$ANA_NUM"

# ─────────────────────────────────────────────────────────────
section "APERTURA DE CUENTAS ADICIONALES"
# ─────────────────────────────────────────────────────────────

# Nueva cuenta CORRIENTE
http "$COOKIE" "" POST /api/v1/cuentas/abrir \
  -H "Content-Type: application/json" \
  -d '{"tipoCuenta":"CORRIENTE"}'
assert "Abrir cuenta CORRIENTE → 201" "201" "$RESP_CODE" "$RESP_BODY"
assert_contains "  respuesta contiene numeroCuenta" "numeroCuenta" "$RESP_BODY" "$RESP_CODE"
CTA2_ID=$(echo "$RESP_BODY"  | grep -o '"idCuenta":[0-9]*' | head -1 | cut -d: -f2)
CTA2_NUM=$(echo "$RESP_BODY" | grep -o '"numeroCuenta":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "  Cuenta 2 (CORRIENTE): id=$CTA2_ID  num=$CTA2_NUM"

# Nueva cuenta AHORROS
http "$COOKIE" "" POST /api/v1/cuentas/abrir \
  -H "Content-Type: application/json" \
  -d '{"tipoCuenta":"AHORROS"}'
assert "Abrir segunda cuenta AHORROS → 201" "201" "$RESP_CODE" "$RESP_BODY"
CTA3_ID=$(echo "$RESP_BODY"  | grep -o '"idCuenta":[0-9]*' | head -1 | cut -d: -f2)
CTA3_NUM=$(echo "$RESP_BODY" | grep -o '"numeroCuenta":"[^"]*"' | head -1 | cut -d'"' -f4)
echo "  Cuenta 3 (AHORROS):   id=$CTA3_ID  num=$CTA3_NUM"

# Verificar que el dashboard ahora muestra 3 cuentas
http "$COOKIE" "" GET /api/v1/cuentas/dashboard
TOTAL=$(echo "$RESP_BODY" | grep -o '"idCuenta"' | wc -l)
if [ "$TOTAL" -ge 3 ]; then
  color_ok "  Dashboard muestra $TOTAL cuentas de Bryan"
  ((PASS++))
else
  color_fail "  Dashboard muestra solo $TOTAL cuenta(s) (esperado ≥3)"
  ((FAIL++))
fi

# ─────────────────────────────────────────────────────────────
section "MOVIMIENTOS — Cuenta 1 (AHORROS original, $CTA1_NUM)"
# ─────────────────────────────────────────────────────────────

subsection "DEPÓSITO"
http "$COOKIE" "" POST /api/v1/transacciones/depositar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$CTA1_ID,\"monto\":500000}"
assert "Depositar 500,000 en cta1" "200" "$RESP_CODE" "$RESP_BODY"
assert_contains "  saldoResultante presente" "saldoResultante" "$RESP_BODY" "$RESP_CODE"

subsection "RETIRO"
http "$COOKIE" "" POST /api/v1/transacciones/retirar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$CTA1_ID,\"monto\":50000}"
assert "Retirar 50,000 de cta1" "200" "$RESP_CODE" "$RESP_BODY"

subsection "TRANSFERENCIA INTERNA → cta2"
http "$COOKIE" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$CTA1_ID,\"numeroCuentaDestino\":\"$CTA2_NUM\",\"monto\":100000}"
assert "Transferir 100,000 cta1 → cta2" "200" "$RESP_CODE" "$RESP_BODY"

subsection "TRANSFERENCIA INTERNA → cta3"
http "$COOKIE" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$CTA1_ID,\"numeroCuentaDestino\":\"$CTA3_NUM\",\"monto\":75000}"
assert "Transferir 75,000 cta1 → cta3" "200" "$RESP_CODE" "$RESP_BODY"

subsection "TRANSFERENCIA → cuenta de Ana"
http "$COOKIE" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$CTA1_ID,\"numeroCuentaDestino\":\"$ANA_NUM\",\"monto\":25000}"
assert "Transferir 25,000 cta1 → Ana" "200" "$RESP_CODE" "$RESP_BODY"

subsection "ACH — transferencia interbancaria (confirmar)"
http "$COOKIE" "" POST /api/v1/transferencias/interbancarias \
  -H "Content-Type: application/json" \
  -d "{
    \"idCuentaOrigen\":$CTA1_ID,
    \"bancoDestino\":\"Bancolombia\",
    \"numeroCuentaDestino\":\"11122233344\",
    \"tipoCuentaDestino\":\"AHORROS\",
    \"tipoDocumentoReceptor\":\"CC\",
    \"numeroDocumentoReceptor\":\"1001001001\",
    \"nombreReceptor\":\"Receptor Bancolombia\",
    \"monto\":30000
  }"
assert "ACH cta1 → Bancolombia 30,000" "200" "$RESP_CODE" "$RESP_BODY"
ACH1=$(echo "$RESP_BODY" | grep -o '"idTransaccion":[0-9]*' | head -1 | cut -d: -f2)
echo "  → ACH id=$ACH1"

if [ -n "$ACH1" ]; then
  http "" "" POST /api/v1/transferencias/interbancarias/$ACH1/confirmacion-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"referenciaConfirmacion":"ACH-CTA1-CONFIRM-001"}'
  assert "  Confirmar ACH cta1" "200" "$RESP_CODE" "$RESP_BODY"
fi

subsection "ACH — transferencia interbancaria (rechazar)"
http "$COOKIE" "" POST /api/v1/transferencias/interbancarias \
  -H "Content-Type: application/json" \
  -d "{
    \"idCuentaOrigen\":$CTA1_ID,
    \"bancoDestino\":\"Davivienda\",
    \"numeroCuentaDestino\":\"99988877766\",
    \"tipoCuentaDestino\":\"CORRIENTE\",
    \"tipoDocumentoReceptor\":\"CC\",
    \"numeroDocumentoReceptor\":\"2002002002\",
    \"nombreReceptor\":\"Receptor Davivienda\",
    \"monto\":20000
  }"
assert "ACH cta1 → Davivienda 20,000 (para rechazo)" "200" "$RESP_CODE" "$RESP_BODY"
ACH1R=$(echo "$RESP_BODY" | grep -o '"idTransaccion":[0-9]*' | head -1 | cut -d: -f2)

if [ -n "$ACH1R" ]; then
  http "" "" POST /api/v1/transferencias/interbancarias/$ACH1R/rechazo-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"motivo":"Cuenta destino no encontrada ACH_ERR_01"}'
  assert "  Rechazar ACH cta1 (reversión automática)" "200" "$RESP_CODE" "$RESP_BODY"
fi

subsection "HISTORIAL cta1"
http "$COOKIE" "" GET /api/v1/transacciones/cuenta/$CTA1_ID
assert "Historial cta1 → 200" "200" "$RESP_CODE" "$RESP_BODY"
TOTAL_TX=$(echo "$RESP_BODY" | grep -o '"idTransaccion"' | wc -l)
color_ok "  $TOTAL_TX transacciones registradas en cta1"

# ─────────────────────────────────────────────────────────────
section "MOVIMIENTOS — Cuenta 2 (CORRIENTE, $CTA2_NUM)"
# ─────────────────────────────────────────────────────────────

subsection "DEPÓSITO (dinero inicial)"
http "$COOKIE" "" POST /api/v1/transacciones/depositar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$CTA2_ID,\"monto\":300000}"
assert "Depositar 300,000 en cta2" "200" "$RESP_CODE" "$RESP_BODY"

subsection "RETIRO"
http "$COOKIE" "" POST /api/v1/transacciones/retirar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$CTA2_ID,\"monto\":20000}"
assert "Retirar 20,000 de cta2" "200" "$RESP_CODE" "$RESP_BODY"

subsection "TRANSFERENCIA INTERNA → cta1"
http "$COOKIE" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$CTA2_ID,\"numeroCuentaDestino\":\"$CTA1_NUM\",\"monto\":50000}"
assert "Transferir 50,000 cta2 → cta1" "200" "$RESP_CODE" "$RESP_BODY"

subsection "TRANSFERENCIA INTERNA → cta3"
http "$COOKIE" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$CTA2_ID,\"numeroCuentaDestino\":\"$CTA3_NUM\",\"monto\":40000}"
assert "Transferir 40,000 cta2 → cta3" "200" "$RESP_CODE" "$RESP_BODY"

subsection "TRANSFERENCIA → cuenta de Ana"
http "$COOKIE" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$CTA2_ID,\"numeroCuentaDestino\":\"$ANA_NUM\",\"monto\":10000}"
assert "Transferir 10,000 cta2 → Ana" "200" "$RESP_CODE" "$RESP_BODY"

subsection "ACH — transferencia interbancaria (confirmar)"
http "$COOKIE" "" POST /api/v1/transferencias/interbancarias \
  -H "Content-Type: application/json" \
  -d "{
    \"idCuentaOrigen\":$CTA2_ID,
    \"bancoDestino\":\"BBVA\",
    \"numeroCuentaDestino\":\"55566677788\",
    \"tipoCuentaDestino\":\"CORRIENTE\",
    \"tipoDocumentoReceptor\":\"CC\",
    \"numeroDocumentoReceptor\":\"3003003003\",
    \"nombreReceptor\":\"Receptor BBVA\",
    \"monto\":15000
  }"
assert "ACH cta2 → BBVA 15,000" "200" "$RESP_CODE" "$RESP_BODY"
ACH2=$(echo "$RESP_BODY" | grep -o '"idTransaccion":[0-9]*' | head -1 | cut -d: -f2)

if [ -n "$ACH2" ]; then
  http "" "" POST /api/v1/transferencias/interbancarias/$ACH2/confirmacion-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"referenciaConfirmacion":"ACH-CTA2-CONFIRM-001"}'
  assert "  Confirmar ACH cta2" "200" "$RESP_CODE" "$RESP_BODY"
fi

subsection "ACH — transferencia interbancaria (rechazar)"
http "$COOKIE" "" POST /api/v1/transferencias/interbancarias \
  -H "Content-Type: application/json" \
  -d "{
    \"idCuentaOrigen\":$CTA2_ID,
    \"bancoDestino\":\"Nequi\",
    \"numeroCuentaDestino\":\"31000000001\",
    \"tipoCuentaDestino\":\"AHORROS\",
    \"tipoDocumentoReceptor\":\"CC\",
    \"numeroDocumentoReceptor\":\"4004004004\",
    \"nombreReceptor\":\"Receptor Nequi\",
    \"monto\":12000
  }"
assert "ACH cta2 → Nequi 12,000 (para rechazo)" "200" "$RESP_CODE" "$RESP_BODY"
ACH2R=$(echo "$RESP_BODY" | grep -o '"idTransaccion":[0-9]*' | head -1 | cut -d: -f2)

if [ -n "$ACH2R" ]; then
  http "" "" POST /api/v1/transferencias/interbancarias/$ACH2R/rechazo-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"motivo":"Número de cuenta Nequi inválido ACH_ERR_02"}'
  assert "  Rechazar ACH cta2 (reversión)" "200" "$RESP_CODE" "$RESP_BODY"
fi

subsection "HISTORIAL cta2"
http "$COOKIE" "" GET /api/v1/transacciones/cuenta/$CTA2_ID
assert "Historial cta2 → 200" "200" "$RESP_CODE" "$RESP_BODY"
TOTAL_TX=$(echo "$RESP_BODY" | grep -o '"idTransaccion"' | wc -l)
color_ok "  $TOTAL_TX transacciones registradas en cta2"

# ─────────────────────────────────────────────────────────────
section "MOVIMIENTOS — Cuenta 3 (AHORROS nueva, $CTA3_NUM)"
# ─────────────────────────────────────────────────────────────

subsection "DEPÓSITO (dinero inicial)"
http "$COOKIE" "" POST /api/v1/transacciones/depositar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$CTA3_ID,\"monto\":800000}"
assert "Depositar 800,000 en cta3" "200" "$RESP_CODE" "$RESP_BODY"

subsection "RETIRO"
http "$COOKIE" "" POST /api/v1/transacciones/retirar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$CTA3_ID,\"monto\":100000}"
assert "Retirar 100,000 de cta3" "200" "$RESP_CODE" "$RESP_BODY"

subsection "TRANSFERENCIA INTERNA → cta1"
http "$COOKIE" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$CTA3_ID,\"numeroCuentaDestino\":\"$CTA1_NUM\",\"monto\":60000}"
assert "Transferir 60,000 cta3 → cta1" "200" "$RESP_CODE" "$RESP_BODY"

subsection "TRANSFERENCIA INTERNA → cta2"
http "$COOKIE" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$CTA3_ID,\"numeroCuentaDestino\":\"$CTA2_NUM\",\"monto\":45000}"
assert "Transferir 45,000 cta3 → cta2" "200" "$RESP_CODE" "$RESP_BODY"

subsection "TRANSFERENCIA → cuenta de Ana"
http "$COOKIE" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$CTA3_ID,\"numeroCuentaDestino\":\"$ANA_NUM\",\"monto\":35000}"
assert "Transferir 35,000 cta3 → Ana" "200" "$RESP_CODE" "$RESP_BODY"

subsection "ACH — transferencia interbancaria (confirmar)"
http "$COOKIE" "" POST /api/v1/transferencias/interbancarias \
  -H "Content-Type: application/json" \
  -d "{
    \"idCuentaOrigen\":$CTA3_ID,
    \"bancoDestino\":\"Bancolombia\",
    \"numeroCuentaDestino\":\"77788899900\",
    \"tipoCuentaDestino\":\"AHORROS\",
    \"tipoDocumentoReceptor\":\"CC\",
    \"numeroDocumentoReceptor\":\"5005005005\",
    \"nombreReceptor\":\"Receptor Bancolombia 2\",
    \"monto\":50000
  }"
assert "ACH cta3 → Bancolombia 50,000" "200" "$RESP_CODE" "$RESP_BODY"
ACH3=$(echo "$RESP_BODY" | grep -o '"idTransaccion":[0-9]*' | head -1 | cut -d: -f2)

if [ -n "$ACH3" ]; then
  http "" "" POST /api/v1/transferencias/interbancarias/$ACH3/confirmacion-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"referenciaConfirmacion":"ACH-CTA3-CONFIRM-001"}'
  assert "  Confirmar ACH cta3" "200" "$RESP_CODE" "$RESP_BODY"
fi

subsection "ACH — transferencia interbancaria (rechazar)"
http "$COOKIE" "" POST /api/v1/transferencias/interbancarias \
  -H "Content-Type: application/json" \
  -d "{
    \"idCuentaOrigen\":$CTA3_ID,
    \"bancoDestino\":\"Scotiabank\",
    \"numeroCuentaDestino\":\"12312312312\",
    \"tipoCuentaDestino\":\"CORRIENTE\",
    \"tipoDocumentoReceptor\":\"CC\",
    \"numeroDocumentoReceptor\":\"6006006006\",
    \"nombreReceptor\":\"Receptor Scotiabank\",
    \"monto\":18000
  }"
assert "ACH cta3 → Scotiabank 18,000 (para rechazo)" "200" "$RESP_CODE" "$RESP_BODY"
ACH3R=$(echo "$RESP_BODY" | grep -o '"idTransaccion":[0-9]*' | head -1 | cut -d: -f2)

if [ -n "$ACH3R" ]; then
  http "" "" POST /api/v1/transferencias/interbancarias/$ACH3R/rechazo-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"motivo":"Banco destino no disponible ACH_ERR_03"}'
  assert "  Rechazar ACH cta3 (reversión)" "200" "$RESP_CODE" "$RESP_BODY"
fi

subsection "HISTORIAL cta3"
http "$COOKIE" "" GET /api/v1/transacciones/cuenta/$CTA3_ID
assert "Historial cta3 → 200" "200" "$RESP_CODE" "$RESP_BODY"
TOTAL_TX=$(echo "$RESP_BODY" | grep -o '"idTransaccion"' | wc -l)
color_ok "  $TOTAL_TX transacciones registradas en cta3"

# ─────────────────────────────────────────────────────────────
section "ESTADO FINAL — Dashboard de Bryan"
# ─────────────────────────────────────────────────────────────

http "$COOKIE" "" GET /api/v1/cuentas/dashboard
assert "Dashboard final → 200" "200" "$RESP_CODE" "$RESP_BODY"

echo ""
echo "  Cuentas de Bryan:"
echo "$RESP_BODY" | grep -o '"numeroCuenta":"[^"]*"\|"tipo":"[^"]*"\|"saldo":[0-9.]*\|"estado":"[^"]*"' \
  | paste - - - - | while IFS=$'\t' read num tipo saldo estado; do
    echo "    $num  $tipo  saldo=$saldo  $estado"
  done

# ─────────────────────────────────────────────────────────────
echo ""
echo "══════════════════════════════════════════"
echo " RESULTADO: $PASS pasaron | $FAIL fallaron"
echo "══════════════════════════════════════════"

rm -f "$COOKIE" "$COOKIE_ANA" /tmp/rb.txt
[ "$FAIL" -eq 0 ] && exit 0 || exit 1
