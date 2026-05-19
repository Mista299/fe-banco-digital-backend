#!/bin/bash
# ============================================================
# Prueba integral de todos los endpoints del backend
# Uso: ./scripts/test-all-endpoints.sh
# ============================================================

BASE="http://localhost:8080"
COOKIE_BRYAN="/tmp/cookies_bryan.txt"
COOKIE_ANA="/tmp/cookies_ana.txt"
COOKIE_TEMP="/tmp/cookies_temp.txt"
GATEWAY_SECRET="clave_secreta_pasarela_banco_2026_hmac"

PASS=0
FAIL=0
WARN=0

# ── Helpers ─────────────────────────────────────────────────
color_ok()   { echo -e "\033[0;32m  OK\033[0m  $1"; }
color_fail() { echo -e "\033[0;31m  KO\033[0m  $1"; }
color_warn() { echo -e "\033[1;33m WARN\033[0m $1"; }
section()    { echo -e "\n\033[1;34m══ $1 ══\033[0m"; }

assert() {
  local label="$1"
  local expected="$2"
  local actual="$3"
  local body="$4"
  if [ "$actual" = "$expected" ]; then
    color_ok "[$actual] $label"
    ((PASS++))
  else
    color_fail "[$actual] $label  (esperado $expected)"
    [ -n "$body" ] && echo "        body: $(echo "$body" | head -c 200)"
    ((FAIL++))
  fi
}

assert_contains() {
  local label="$1"
  local needle="$2"
  local haystack="$3"
  local status="$4"
  if echo "$haystack" | grep -qF "$needle"; then
    color_ok "[$status] $label"
    ((PASS++))
  else
    color_fail "[$status] $label  (no contiene: $needle)"
    echo "        body: $(echo "$haystack" | head -c 200)"
    ((FAIL++))
  fi
}

http() {
  # http <cookie_jar_r> <cookie_jar_w> <method> <path> [extra_curl_args...]
  local jar_r="$1"; shift
  local jar_w="$1"; shift
  local method="$1"; shift
  local path="$1"; shift
  local args=("$@")
  local cmd=(curl -s -o /tmp/resp_body.txt -w "%{http_code}"
    -X "$method"
    "${BASE}${path}"
    "${args[@]}")
  [ -n "$jar_r" ] && cmd+=(-b "$jar_r")
  [ -n "$jar_w" ] && cmd+=(-c "$jar_w")
  local code
  code=$("${cmd[@]}")
  RESP_CODE="$code"
  RESP_BODY=$(cat /tmp/resp_body.txt)
}

# ── SETUP: login bryan y ana ─────────────────────────────────
section "SETUP — Login usuarios semilla"

http "" "$COOKIE_BRYAN" POST /api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"bryan","password":"bryan123"}'
assert "Login bryan" "200" "$RESP_CODE" "$RESP_BODY"
BRYAN_OK=$( [ "$RESP_CODE" = "200" ] && echo "yes" || echo "no" )

http "" "$COOKIE_ANA" POST /api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ana","password":"ana123"}'
assert "Login ana" "200" "$RESP_CODE" "$RESP_BODY"

if [ "$BRYAN_OK" != "yes" ]; then
  echo ""
  echo "  Login de bryan falló — el resto de pruebas autenticadas no puede continuar."
  echo "  ¿Está el servidor corriendo con --spring.profiles.active=seed?"
  exit 1
fi

# Obtener idCuenta de bryan dinámicamente desde el dashboard
http "$COOKIE_BRYAN" "" GET /api/v1/cuentas/dashboard
BRYAN_CUENTA_ID=$(echo "$RESP_BODY" | grep -o '"idCuenta":[0-9]*' | head -1 | cut -d: -f2)
BRYAN_CUENTA_NUM=$(echo "$RESP_BODY" | grep -o '"numeroCuenta":"[^"]*"' | head -1 | cut -d'"' -f4)

http "$COOKIE_ANA" "" GET /api/v1/cuentas/dashboard
ANA_CUENTA_ID=$(echo "$RESP_BODY" | grep -o '"idCuenta":[0-9]*' | head -1 | cut -d: -f2)

echo "  → Bryan cuenta id=$BRYAN_CUENTA_ID  num=$BRYAN_CUENTA_NUM"
echo "  → Ana   cuenta id=$ANA_CUENTA_ID"

# ── 1. PÚBLICO — ping y registro ───────────────────────────
section "1. ENDPOINTS PÚBLICOS"

http "" "" GET /api/db/ping
assert "GET /api/db/ping" "200" "$RESP_CODE" "$RESP_BODY"
assert_contains "  db ping retorna ok=1" '"ok":1' "$RESP_BODY" "$RESP_CODE"

# registro/validar-identidad — documento nuevo
http "" "" POST /api/v1/registro/validar-identidad \
  -H "Content-Type: application/json" \
  -d '{"documento":"999888777","fechaExpedicion":"2000-01-01"}'
assert "POST /api/v1/registro/validar-identidad (doc nuevo)" "200" "$RESP_CODE" "$RESP_BODY"

# registro/validar-identidad — documento existente (bryan: 123456789)
http "" "" POST /api/v1/registro/validar-identidad \
  -H "Content-Type: application/json" \
  -d '{"documento":"123456789","fechaExpedicion":"2020-01-10"}'
assert "POST /api/v1/registro/validar-identidad (doc duplicado → 409)" "409" "$RESP_CODE" "$RESP_BODY"

# registro completo con datos únicos (sufijo de timestamp para evitar colisiones entre corridas)
TS=$(date +%s)
http "" "" POST /api/v1/registro \
  -H "Content-Type: application/json" \
  -d "{
    \"documento\":\"T${TS}\",\"fechaExpedicion\":\"1995-03-15\",
    \"nombre\":\"Test Usuario\",\"email\":\"testuser${TS}@mail.com\",
    \"direccion\":\"Calle Test 1\",\"telefono\":\"3001111111\",
    \"username\":\"testusr${TS}\",\"password\":\"Test123!\"
  }"
assert "POST /api/v1/registro (nuevo usuario)" "201" "$RESP_CODE" "$RESP_BODY"
assert_contains "  respuesta contiene idCuenta" "idCuenta" "$RESP_BODY" "$RESP_CODE"

# registro con username duplicado
http "" "" POST /api/v1/registro \
  -H "Content-Type: application/json" \
  -d "{
    \"documento\":\"T${TS}2\",\"fechaExpedicion\":\"1995-03-15\",
    \"nombre\":\"Test2\",\"email\":\"testuser${TS}2@mail.com\",
    \"direccion\":\"Calle 2\",\"telefono\":\"3002222222\",
    \"username\":\"bryan\",\"password\":\"Test123!\"
  }"
assert "POST /api/v1/registro (username duplicado → 409)" "409" "$RESP_CODE" "$RESP_BODY"

# POST /api/v1/auth/registro (endpoint legacy del AutenticacionController)
http "" "$COOKIE_TEMP" POST /api/v1/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"username":"legacyusr01","password":"legacy123"}'
LEGACY_CODE=$RESP_CODE
if [ "$LEGACY_CODE" = "201" ] || [ "$LEGACY_CODE" = "409" ] || [ "$LEGACY_CODE" = "400" ]; then
  color_ok "[$LEGACY_CODE] POST /api/v1/auth/registro (endpoint legacy)"
  ((PASS++))
else
  color_fail "[$LEGACY_CODE] POST /api/v1/auth/registro (legacy — inesperado)"
  ((FAIL++))
fi

# ── 2. AUTH ─────────────────────────────────────────────────
section "2. AUTENTICACIÓN"

# login credenciales incorrectas
http "" "$COOKIE_TEMP" POST /api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"bryan","password":"wrongpass"}'
assert "POST /api/v1/auth/login (contraseña incorrecta → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# refresh — primero hacer login limpio para tener cookie refresh
http "" "$COOKIE_TEMP" POST /api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ana","password":"ana123"}'
http "$COOKIE_TEMP" "$COOKIE_TEMP" POST /api/v1/auth/refresh
assert "POST /api/v1/auth/refresh (con cookie válida)" "200" "$RESP_CODE" "$RESP_BODY"

# refresh sin cookie
http "" "" POST /api/v1/auth/refresh
assert "POST /api/v1/auth/refresh (sin cookie → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# logout
http "$COOKIE_TEMP" "$COOKIE_TEMP" POST /api/v1/auth/logout
assert "POST /api/v1/auth/logout" "200" "$RESP_CODE" "$RESP_BODY"

# ── 3. PERFIL ───────────────────────────────────────────────
section "3. PERFIL"

http "$COOKIE_BRYAN" "" GET /api/v1/perfil/me
assert "GET /api/v1/perfil/me (autenticado)" "200" "$RESP_CODE" "$RESP_BODY"
assert_contains "  respuesta contiene fullName" "fullName" "$RESP_BODY" "$RESP_CODE"

http "" "" GET /api/v1/perfil/me
assert "GET /api/v1/perfil/me (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# Legacy profile endpoint
http "" "" GET /api/profile/1
LEGACY_PROF=$RESP_CODE
if [ "$LEGACY_PROF" = "200" ] || [ "$LEGACY_PROF" = "401" ] || [ "$LEGACY_PROF" = "404" ]; then
  color_ok "[$LEGACY_PROF] GET /api/profile/1 (legacy)"
  ((PASS++))
else
  color_fail "[$LEGACY_PROF] GET /api/profile/1 (legacy — inesperado)"
  ((FAIL++))
fi

# ── 4. CLIENTES ─────────────────────────────────────────────
section "4. CLIENTES"

http "$COOKIE_BRYAN" "" PUT /api/v1/clientes/me \
  -H "Content-Type: application/json" \
  -d '{"email":"bryan_updated@example.com","telefono":"3009999999","direccion":"Calle Nueva 99"}'
assert "PUT /api/v1/clientes/me" "200" "$RESP_CODE" "$RESP_BODY"

# Restaurar email original para no romper datos semilla
http "$COOKIE_BRYAN" "" PUT /api/v1/clientes/me \
  -H "Content-Type: application/json" \
  -d '{"email":"bryan@example.com","telefono":"3000000001","direccion":"Calle 10 #20-30"}'

http "" "" PUT /api/v1/clientes/me \
  -H "Content-Type: application/json" \
  -d '{"email":"x@x.com","telefono":"123","direccion":"x"}'
assert "PUT /api/v1/clientes/me (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# ── 5. CUENTAS ──────────────────────────────────────────────
section "5. CUENTAS"

http "$COOKIE_BRYAN" "" GET /api/v1/cuentas/dashboard
assert "GET /api/v1/cuentas/dashboard" "200" "$RESP_CODE" "$RESP_BODY"
assert_contains "  respuesta es array" '[' "$RESP_BODY" "$RESP_CODE"

http "" "" GET /api/v1/cuentas/dashboard
assert "GET /api/v1/cuentas/dashboard (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# cerrar cuenta — cuenta con saldo (debería ser 409)
http "$COOKIE_BRYAN" "" PATCH /api/v1/cuentas/cerrar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"contrasena\":\"bryan123\"}"
assert "PATCH /api/v1/cuentas/cerrar (saldo > 0 → 409)" "409" "$RESP_CODE" "$RESP_BODY"

# cerrar cuenta — contraseña incorrecta
http "$COOKIE_BRYAN" "" PATCH /api/v1/cuentas/cerrar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"contrasena\":\"wrongpass\"}"
# Puede ser 401 o 409 dependiendo del orden de validaciones
if [ "$RESP_CODE" = "401" ] || [ "$RESP_CODE" = "409" ]; then
  color_ok "[$RESP_CODE] PATCH /api/v1/cuentas/cerrar (contraseña incorrecta)"
  ((PASS++))
else
  color_fail "[$RESP_CODE] PATCH /api/v1/cuentas/cerrar (esperado 401 o 409)"
  ((FAIL++))
fi

# ── 6. SEGURIDAD DE CUENTA ──────────────────────────────────
section "6. SEGURIDAD DE CUENTA"

http "$COOKIE_BRYAN" "" POST /api/v1/cuentas/seguridad/bloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"bryan123"}'
assert "POST /api/v1/cuentas/seguridad/bloquear" "200" "$RESP_CODE" "$RESP_BODY"

http "$COOKIE_BRYAN" "" POST /api/v1/cuentas/seguridad/desbloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"bryan123"}'
assert "POST /api/v1/cuentas/seguridad/desbloquear" "200" "$RESP_CODE" "$RESP_BODY"

http "$COOKIE_BRYAN" "" POST /api/v1/cuentas/seguridad/bloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"wrongpass"}'
# Esperamos 401 o 403
if [ "$RESP_CODE" = "401" ] || [ "$RESP_CODE" = "403" ] || [ "$RESP_CODE" = "400" ]; then
  color_ok "[$RESP_CODE] POST /api/v1/cuentas/seguridad/bloquear (contraseña incorrecta)"
  ((PASS++))
else
  color_fail "[$RESP_CODE] POST /api/v1/cuentas/seguridad/bloquear (contraseña incorrecta — inesperado)"
  echo "        body: $(echo "$RESP_BODY" | head -c 200)"
  ((FAIL++))
fi

# ── 7. TRANSACCIONES ────────────────────────────────────────
section "7. TRANSACCIONES"

http "$COOKIE_BRYAN" "" GET /api/v1/transacciones/cuenta/$BRYAN_CUENTA_ID
assert "GET /api/v1/transacciones/cuenta/{id} (propio)" "200" "$RESP_CODE" "$RESP_BODY"
assert_contains "  respuesta es array" '[' "$RESP_BODY" "$RESP_CODE"

# acceso a cuenta ajena
http "$COOKIE_BRYAN" "" GET /api/v1/transacciones/cuenta/$ANA_CUENTA_ID
assert "GET /api/v1/transacciones/cuenta/{id} (ajena → 403)" "403" "$RESP_CODE" "$RESP_BODY"

# filtro por fecha
FECHA_INI="2026-01-01T00:00:00"
FECHA_FIN="2026-12-31T23:59:59"
http "$COOKIE_BRYAN" "" GET "/api/v1/transacciones/cuenta/$BRYAN_CUENTA_ID/filtro?fechaInicio=$FECHA_INI&fechaFin=$FECHA_FIN"
assert "GET /api/v1/transacciones/cuenta/{id}/filtro" "200" "$RESP_CODE" "$RESP_BODY"

# depositar
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/depositar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"monto\":10000}"
assert "POST /api/v1/transacciones/depositar" "200" "$RESP_CODE" "$RESP_BODY"
assert_contains "  respuesta tiene idTransaccion" "idTransaccion" "$RESP_BODY" "$RESP_CODE"

# depositar en cuenta ajena
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/depositar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$ANA_CUENTA_ID,\"monto\":10000}"
assert "POST /api/v1/transacciones/depositar (cuenta ajena → 403)" "403" "$RESP_CODE" "$RESP_BODY"

# retirar
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/retirar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"monto\":5000}"
assert "POST /api/v1/transacciones/retirar" "200" "$RESP_CODE" "$RESP_BODY"

# retirar más de lo disponible
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/retirar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"monto\":99999999}"
assert "POST /api/v1/transacciones/retirar (saldo insuficiente → 409)" "409" "$RESP_CODE" "$RESP_BODY"

# transferir
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$BRYAN_CUENTA_ID,\"numeroCuentaDestino\":\"$( echo "$RESP_BODY" | head -c 0 )\"}"
# necesitamos el numeroCuenta de ana, lo buscamos
http "$COOKIE_ANA" "" GET /api/v1/cuentas/dashboard
ANA_CUENTA_NUM=$(echo "$RESP_BODY" | grep -o '"numeroCuenta":"[^"]*"' | head -1 | cut -d'"' -f4)

http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$BRYAN_CUENTA_ID,\"numeroCuentaDestino\":\"$ANA_CUENTA_NUM\",\"monto\":1000}"
assert "POST /api/v1/transacciones/transferir" "200" "$RESP_CODE" "$RESP_BODY"

# transferir a cuenta inexistente
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$BRYAN_CUENTA_ID,\"numeroCuentaDestino\":\"00000000\",\"monto\":1000}"
assert "POST /api/v1/transacciones/transferir (destino inexistente → 404)" "404" "$RESP_CODE" "$RESP_BODY"

# ── 8. VALIDACIONES ─────────────────────────────────────────
section "8. MOTOR DE VALIDACIÓN"

http "$COOKIE_BRYAN" "" POST /api/v1/validaciones/transaccion \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$BRYAN_CUENTA_ID,\"monto\":100,\"tipoOperacion\":\"RETIRO\"}"
assert "POST /api/v1/validaciones/transaccion (autorizada)" "200" "$RESP_CODE" "$RESP_BODY"
assert_contains "  respuesta tiene autorizada" "autorizada" "$RESP_BODY" "$RESP_CODE"

http "$COOKIE_BRYAN" "" POST /api/v1/validaciones/transaccion \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$BRYAN_CUENTA_ID,\"monto\":99999999,\"tipoOperacion\":\"RETIRO\"}"
assert "POST /api/v1/validaciones/transaccion (no autorizada — saldo)" "200" "$RESP_CODE" "$RESP_BODY"

# ── 9. TRANSFERENCIAS INTERBANCARIAS ────────────────────────
section "9. TRANSFERENCIAS INTERBANCARIAS (ACH)"

http "$COOKIE_BRYAN" "" POST /api/v1/transferencias/interbancarias \
  -H "Content-Type: application/json" \
  -d "{
    \"idCuentaOrigen\":$BRYAN_CUENTA_ID,
    \"bancoDestino\":\"Bancolombia\",
    \"numeroCuentaDestino\":\"123456789\",
    \"tipoCuentaDestino\":\"AHORROS\",
    \"tipoDocumentoReceptor\":\"CC\",
    \"numeroDocumentoReceptor\":\"1122334455\",
    \"nombreReceptor\":\"Test Receptor\",
    \"monto\":20000
  }"
assert "POST /api/v1/transferencias/interbancarias" "200" "$RESP_CODE" "$RESP_BODY"
TX_ACH_ID=$(echo "$RESP_BODY" | grep -o '"idTransaccion":[0-9]*' | head -1 | cut -d: -f2)
echo "  → idTransaccion ACH creada: $TX_ACH_ID"

if [ -n "$TX_ACH_ID" ]; then
  # confirmacion-ach (no requiere auth de usuario, solo gateway secret)
  http "" "" POST /api/v1/transferencias/interbancarias/$TX_ACH_ID/confirmacion-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"referenciaConfirmacion":"ACH-TEST-CONFIRM-001"}'
  assert "POST /confirmacion-ach" "200" "$RESP_CODE" "$RESP_BODY"

  # rechazo sobre transacción ya confirmada (debe fallar)
  http "" "" POST /api/v1/transferencias/interbancarias/$TX_ACH_ID/rechazo-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"motivo":"prueba doble operacion"}'
  assert "POST /rechazo-ach sobre ya confirmada → 400" "400" "$RESP_CODE" "$RESP_BODY"
else
  color_warn "  No se pudo crear la ACH → se omiten pruebas de confirmacion/rechazo"
  ((WARN++))
fi

# flujo rechazo completo
http "$COOKIE_BRYAN" "" POST /api/v1/transferencias/interbancarias \
  -H "Content-Type: application/json" \
  -d "{
    \"idCuentaOrigen\":$BRYAN_CUENTA_ID,
    \"bancoDestino\":\"Davivienda\",
    \"numeroCuentaDestino\":\"987654321\",
    \"tipoCuentaDestino\":\"CORRIENTE\",
    \"tipoDocumentoReceptor\":\"CC\",
    \"numeroDocumentoReceptor\":\"9988776655\",
    \"nombreReceptor\":\"Receptor Rechazo\",
    \"monto\":15000
  }"
assert "POST /api/v1/transferencias/interbancarias (para rechazo)" "200" "$RESP_CODE" "$RESP_BODY"
TX_ACH_REJ_ID=$(echo "$RESP_BODY" | grep -o '"idTransaccion":[0-9]*' | head -1 | cut -d: -f2)

if [ -n "$TX_ACH_REJ_ID" ]; then
  http "" "" POST /api/v1/transferencias/interbancarias/$TX_ACH_REJ_ID/rechazo-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"motivo":"Cuenta destino invalida ACH_01"}'
  assert "POST /rechazo-ach (reversión automática)" "200" "$RESP_CODE" "$RESP_BODY"
fi

# gateway secret incorrecto
if [ -n "$TX_ACH_ID" ]; then
  http "" "" POST /api/v1/transferencias/interbancarias/$TX_ACH_ID/confirmacion-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: wrong_secret" \
    -d '{"referenciaConfirmacion":"ACH-BAD"}'
  assert "POST /confirmacion-ach (secret incorrecto → 403)" "403" "$RESP_CODE" "$RESP_BODY"
fi

# consultar transferencia propia
if [ -n "$TX_ACH_ID" ]; then
  http "$COOKIE_BRYAN" "" GET /api/v1/transferencias/interbancarias/$TX_ACH_ID
  assert "GET /api/v1/transferencias/interbancarias/{id} (propia)" "200" "$RESP_CODE" "$RESP_BODY"

  # consultar transferencia ajena
  http "$COOKIE_ANA" "" GET /api/v1/transferencias/interbancarias/$TX_ACH_ID
  assert "GET /api/v1/transferencias/interbancarias/{id} (ajena → 403)" "403" "$RESP_CODE" "$RESP_BODY"
fi

# sin gateway secret
http "$COOKIE_BRYAN" "" POST /api/v1/transferencias/interbancarias \
  -H "Content-Type: application/json" \
  -d "{
    \"idCuentaOrigen\":$BRYAN_CUENTA_ID,
    \"bancoDestino\":\"Banco\",
    \"numeroCuentaDestino\":\"111\",
    \"tipoCuentaDestino\":\"AHORROS\",
    \"tipoDocumentoReceptor\":\"CC\",
    \"numeroDocumentoReceptor\":\"111\",
    \"nombreReceptor\":\"R\",
    \"monto\":5000
  }"
TX_NO_SECRET=$(echo "$RESP_BODY" | grep -o '"idTransaccion":[0-9]*' | head -1 | cut -d: -f2)
if [ -n "$TX_NO_SECRET" ]; then
  http "" "" POST /api/v1/transferencias/interbancarias/$TX_NO_SECRET/confirmacion-ach \
    -H "Content-Type: application/json" \
    -d '{"referenciaConfirmacion":"ACH-NO-SECRET"}'
  assert "POST /confirmacion-ach (sin header X-Gateway-Secret → 403)" "403" "$RESP_CODE" "$RESP_BODY"
fi

# ── RESUMEN ─────────────────────────────────────────────────
echo ""
echo "══════════════════════════════════════════"
echo " RESULTADO: $PASS pasaron | $FAIL fallaron | $WARN advertencias"
echo "══════════════════════════════════════════"

# Cleanup
rm -f "$COOKIE_BRYAN" "$COOKIE_ANA" "$COOKIE_TEMP" /tmp/resp_body.txt

[ "$FAIL" -eq 0 ] && exit 0 || exit 1
