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

assert_any() {
  # Acepta cualquiera de los códigos listados (separados por |)
  local label="$1"
  local expected_list="$2"   # e.g. "401|403"
  local actual="$3"
  local body="$4"
  if echo "$expected_list" | grep -qF "$actual"; then
    color_ok "[$actual] $label"
    ((PASS++))
  else
    color_fail "[$actual] $label  (esperado uno de: $expected_list)"
    [ -n "$body" ] && echo "        body: $(echo "$body" | head -c 200)"
    ((FAIL++))
  fi
}

http() {
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

# Obtener idCuenta dinámicamente
http "$COOKIE_BRYAN" "" GET /api/v1/cuentas/dashboard
BRYAN_CUENTA_ID=$(echo "$RESP_BODY" | grep -o '"idCuenta":[0-9]*' | head -1 | cut -d: -f2)
BRYAN_CUENTA_NUM=$(echo "$RESP_BODY" | grep -o '"numeroCuenta":"[^"]*"' | head -1 | cut -d'"' -f4)

http "$COOKIE_ANA" "" GET /api/v1/cuentas/dashboard
ANA_CUENTA_ID=$(echo "$RESP_BODY" | grep -o '"idCuenta":[0-9]*' | head -1 | cut -d: -f2)
ANA_CUENTA_NUM=$(echo "$RESP_BODY" | grep -o '"numeroCuenta":"[^"]*"' | head -1 | cut -d'"' -f4)

echo "  → Bryan cuenta id=$BRYAN_CUENTA_ID  num=$BRYAN_CUENTA_NUM"
echo "  → Ana   cuenta id=$ANA_CUENTA_ID  num=$ANA_CUENTA_NUM"

# ── 1. ENDPOINTS PÚBLICOS ────────────────────────────────────
section "1. ENDPOINTS PÚBLICOS"

# DB ping
http "" "" GET /api/db/ping
assert "GET /api/db/ping" "200" "$RESP_CODE" "$RESP_BODY"
assert_contains "  db ping retorna ok=1" '"ok":1' "$RESP_BODY" "$RESP_CODE"

# validar-identidad: documento nuevo
http "" "" POST /api/v1/registro/validar-identidad \
  -H "Content-Type: application/json" \
  -d '{"documento":"999888777","fechaExpedicion":"2000-01-01"}'
assert "POST /api/v1/registro/validar-identidad (doc nuevo → 200)" "200" "$RESP_CODE" "$RESP_BODY"

# validar-identidad: documento existente
http "" "" POST /api/v1/registro/validar-identidad \
  -H "Content-Type: application/json" \
  -d '{"documento":"123456789","fechaExpedicion":"2020-01-10"}'
assert "POST /api/v1/registro/validar-identidad (doc duplicado → 409)" "409" "$RESP_CODE" "$RESP_BODY"

# validar-identidad: campos faltantes
http "" "" POST /api/v1/registro/validar-identidad \
  -H "Content-Type: application/json" \
  -d '{}'
assert "POST /api/v1/registro/validar-identidad (campos vacíos → 400)" "400" "$RESP_CODE" "$RESP_BODY"

# registro completo con datos únicos (timestamp para idempotencia)
TS=$(date +%s)
http "" "" POST /api/v1/registro \
  -H "Content-Type: application/json" \
  -d "{
    \"documento\":\"T${TS}\",\"fechaExpedicion\":\"1995-03-15\",
    \"nombre\":\"Test Usuario\",\"email\":\"testuser${TS}@mail.com\",
    \"direccion\":\"Calle Test 1\",\"telefono\":\"3001111111\",
    \"username\":\"testusr${TS}\",\"password\":\"Test123!\"
  }"
assert "POST /api/v1/registro (nuevo usuario → 201)" "201" "$RESP_CODE" "$RESP_BODY"
assert_contains "  respuesta contiene idCuenta" "idCuenta" "$RESP_BODY" "$RESP_CODE"

# registro: username duplicado
http "" "" POST /api/v1/registro \
  -H "Content-Type: application/json" \
  -d "{
    \"documento\":\"T${TS}2\",\"fechaExpedicion\":\"1995-03-15\",
    \"nombre\":\"Test2\",\"email\":\"testuser${TS}2@mail.com\",
    \"direccion\":\"Calle 2\",\"telefono\":\"3002222222\",
    \"username\":\"bryan\",\"password\":\"Test123!\"
  }"
assert "POST /api/v1/registro (username duplicado → 409)" "409" "$RESP_CODE" "$RESP_BODY"

# /auth/registro legacy: campos faltantes (sin idCliente)
http "" "$COOKIE_TEMP" POST /api/v1/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"username":"legacyusr01","password":"legacy123"}'
assert "POST /api/v1/auth/registro (sin idCliente → 400)" "400" "$RESP_CODE" "$RESP_BODY"

# /auth/registro legacy: idCliente inexistente
http "" "$COOKIE_TEMP" POST /api/v1/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"username":"legacyusr99","password":"legacy123","idCliente":99999}'
assert "POST /api/v1/auth/registro (idCliente inexistente → 404)" "404" "$RESP_CODE" "$RESP_BODY"

# /auth/registro legacy: username ya existente
http "" "$COOKIE_TEMP" POST /api/v1/auth/registro \
  -H "Content-Type: application/json" \
  -d '{"username":"bryan","password":"Test123!","idCliente":1}'
assert "POST /api/v1/auth/registro (username duplicado → 409)" "409" "$RESP_CODE" "$RESP_BODY"

# ── 2. AUTENTICACIÓN ─────────────────────────────────────────
section "2. AUTENTICACIÓN"

# login contraseña incorrecta
http "" "$COOKIE_TEMP" POST /api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"bryan","password":"wrongpass"}'
assert "POST /api/v1/auth/login (contraseña incorrecta → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# login usuario inexistente
http "" "$COOKIE_TEMP" POST /api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"noexiste999","password":"cualquiera"}'
assert "POST /api/v1/auth/login (usuario inexistente → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# refresh con cookie válida
http "" "$COOKIE_TEMP" POST /api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ana","password":"ana123"}'
http "$COOKIE_TEMP" "$COOKIE_TEMP" POST /api/v1/auth/refresh
assert "POST /api/v1/auth/refresh (con cookie válida → 200)" "200" "$RESP_CODE" "$RESP_BODY"

# refresh sin cookie
http "" "" POST /api/v1/auth/refresh
assert "POST /api/v1/auth/refresh (sin cookie → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# logout
http "$COOKIE_TEMP" "$COOKIE_TEMP" POST /api/v1/auth/logout
assert "POST /api/v1/auth/logout → 200" "200" "$RESP_CODE" "$RESP_BODY"

# ── 3. PERFIL ────────────────────────────────────────────────
section "3. PERFIL"

http "$COOKIE_BRYAN" "" GET /api/v1/perfil/me
assert "GET /api/v1/perfil/me (autenticado → 200)" "200" "$RESP_CODE" "$RESP_BODY"
assert_contains "  respuesta contiene fullName" "fullName" "$RESP_BODY" "$RESP_CODE"

http "" "" GET /api/v1/perfil/me
assert "GET /api/v1/perfil/me (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# legacy profile
http "" "" GET /api/profile/1
LEGACY_PROF=$RESP_CODE
if [ "$LEGACY_PROF" = "200" ] || [ "$LEGACY_PROF" = "401" ] || [ "$LEGACY_PROF" = "404" ]; then
  color_ok "[$LEGACY_PROF] GET /api/profile/1 (legacy)"
  ((PASS++))
else
  color_fail "[$LEGACY_PROF] GET /api/profile/1 (legacy — inesperado)"
  ((FAIL++))
fi

# ── 4. CLIENTES ──────────────────────────────────────────────
section "4. CLIENTES"

# actualizar correcto
http "$COOKIE_BRYAN" "" PUT /api/v1/clientes/me \
  -H "Content-Type: application/json" \
  -d '{"email":"bryan_updated@example.com","telefono":"3009999999","direccion":"Calle Nueva 99"}'
assert "PUT /api/v1/clientes/me (correcto → 200)" "200" "$RESP_CODE" "$RESP_BODY"

# restaurar datos originales
http "$COOKIE_BRYAN" "" PUT /api/v1/clientes/me \
  -H "Content-Type: application/json" \
  -d '{"email":"bryan@example.com","telefono":"3000000001","direccion":"Calle 10 #20-30"}'

# sin auth
http "" "" PUT /api/v1/clientes/me \
  -H "Content-Type: application/json" \
  -d '{"email":"x@x.com","telefono":"123","direccion":"x"}'
assert "PUT /api/v1/clientes/me (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# campos vacíos (viola @NotBlank)
http "$COOKIE_BRYAN" "" PUT /api/v1/clientes/me \
  -H "Content-Type: application/json" \
  -d '{"email":"","telefono":""}'
assert "PUT /api/v1/clientes/me (campos vacíos → 400)" "400" "$RESP_CODE" "$RESP_BODY"

# email con formato inválido
http "$COOKIE_BRYAN" "" PUT /api/v1/clientes/me \
  -H "Content-Type: application/json" \
  -d '{"email":"no-es-un-email","telefono":"3001111111"}'
assert "PUT /api/v1/clientes/me (email inválido → 400)" "400" "$RESP_CODE" "$RESP_BODY"

# email duplicado (email de ana)
http "$COOKIE_BRYAN" "" PUT /api/v1/clientes/me \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@example.com","telefono":"3000000001"}'
assert "PUT /api/v1/clientes/me (email duplicado → 409)" "409" "$RESP_CODE" "$RESP_BODY"

# ── 5. CUENTAS ───────────────────────────────────────────────
section "5. CUENTAS"

http "$COOKIE_BRYAN" "" GET /api/v1/cuentas/dashboard
assert "GET /api/v1/cuentas/dashboard (autenticado → 200)" "200" "$RESP_CODE" "$RESP_BODY"
assert_contains "  respuesta es array" '[' "$RESP_BODY" "$RESP_CODE"

http "" "" GET /api/v1/cuentas/dashboard
assert "GET /api/v1/cuentas/dashboard (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# cerrar: saldo > 0
http "$COOKIE_BRYAN" "" PATCH /api/v1/cuentas/cerrar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"contrasena\":\"bryan123\"}"
assert "PATCH /api/v1/cuentas/cerrar (saldo > 0 → 409)" "409" "$RESP_CODE" "$RESP_BODY"

# cerrar: contraseña incorrecta
http "$COOKIE_BRYAN" "" PATCH /api/v1/cuentas/cerrar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"contrasena\":\"wrongpass\"}"
assert_any "PATCH /api/v1/cuentas/cerrar (contraseña incorrecta → 401)" "401|409" "$RESP_CODE" "$RESP_BODY"

# cerrar: sin auth
http "" "" PATCH /api/v1/cuentas/cerrar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"contrasena\":\"bryan123\"}"
assert "PATCH /api/v1/cuentas/cerrar (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# cerrar: cuenta ajena (bryan intenta cerrar la cuenta de ana)
http "$COOKIE_BRYAN" "" PATCH /api/v1/cuentas/cerrar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$ANA_CUENTA_ID,\"contrasena\":\"bryan123\"}"
assert "PATCH /api/v1/cuentas/cerrar (cuenta ajena → 404)" "404" "$RESP_CODE" "$RESP_BODY"

# cerrar: cuenta inexistente
http "$COOKIE_BRYAN" "" PATCH /api/v1/cuentas/cerrar \
  -H "Content-Type: application/json" \
  -d '{"idCuenta":99999,"contrasena":"bryan123"}'
assert "PATCH /api/v1/cuentas/cerrar (cuenta inexistente → 404)" "404" "$RESP_CODE" "$RESP_BODY"

# ── 6. SEGURIDAD DE CUENTA ───────────────────────────────────
section "6. SEGURIDAD DE CUENTA"

# bloquear correcto
http "$COOKIE_BRYAN" "" POST /api/v1/cuentas/seguridad/bloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"bryan123"}'
assert "POST /api/v1/cuentas/seguridad/bloquear (correcto → 200)" "200" "$RESP_CODE" "$RESP_BODY"

# bloquear ya bloqueada (no hay cuenta ACTIVA → 404)
http "$COOKIE_BRYAN" "" POST /api/v1/cuentas/seguridad/bloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"bryan123"}'
assert "POST /api/v1/cuentas/seguridad/bloquear (ya bloqueada → 404)" "404" "$RESP_CODE" "$RESP_BODY"

# desbloquear correcto (cuenta está BLOQUEADA)
http "$COOKIE_BRYAN" "" POST /api/v1/cuentas/seguridad/desbloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"bryan123"}'
assert "POST /api/v1/cuentas/seguridad/desbloquear (correcto → 200)" "200" "$RESP_CODE" "$RESP_BODY"

# desbloquear contraseña incorrecta (cuenta ahora ACTIVA, nada para desbloquear no la hay bloqueada → 404,
# pero antes llega la validación de contraseña si es incorrecta → 401)
http "$COOKIE_BRYAN" "" POST /api/v1/cuentas/seguridad/desbloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"wrongpass"}'
assert "POST /api/v1/cuentas/seguridad/desbloquear (contraseña incorrecta → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# bloquear contraseña incorrecta
http "$COOKIE_BRYAN" "" POST /api/v1/cuentas/seguridad/bloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"wrongpass"}'
assert_any "POST /api/v1/cuentas/seguridad/bloquear (contraseña incorrecta → 401)" "401|403|400" "$RESP_CODE" "$RESP_BODY"

# bloquear sin auth
http "" "" POST /api/v1/cuentas/seguridad/bloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"bryan123"}'
assert "POST /api/v1/cuentas/seguridad/bloquear (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# desbloquear sin auth
http "" "" POST /api/v1/cuentas/seguridad/desbloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"bryan123"}'
assert "POST /api/v1/cuentas/seguridad/desbloquear (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# ── 7. TRANSACCIONES ─────────────────────────────────────────
section "7. TRANSACCIONES"

# historial cuenta propia
http "$COOKIE_BRYAN" "" GET /api/v1/transacciones/cuenta/$BRYAN_CUENTA_ID
assert "GET /transacciones/cuenta/{id} (propia → 200)" "200" "$RESP_CODE" "$RESP_BODY"
assert_contains "  respuesta es array" '[' "$RESP_BODY" "$RESP_CODE"

# historial cuenta ajena
http "$COOKIE_BRYAN" "" GET /api/v1/transacciones/cuenta/$ANA_CUENTA_ID
assert "GET /transacciones/cuenta/{id} (ajena → 403)" "403" "$RESP_CODE" "$RESP_BODY"

# historial sin auth
http "" "" GET /api/v1/transacciones/cuenta/$BRYAN_CUENTA_ID
assert "GET /transacciones/cuenta/{id} (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# filtro por fechas
FECHA_INI="2026-01-01T00:00:00"
FECHA_FIN="2026-12-31T23:59:59"
http "$COOKIE_BRYAN" "" GET "/api/v1/transacciones/cuenta/$BRYAN_CUENTA_ID/filtro?fechaInicio=$FECHA_INI&fechaFin=$FECHA_FIN"
assert "GET /transacciones/cuenta/{id}/filtro (correcto → 200)" "200" "$RESP_CODE" "$RESP_BODY"

# filtro cuenta ajena
http "$COOKIE_BRYAN" "" GET "/api/v1/transacciones/cuenta/$ANA_CUENTA_ID/filtro?fechaInicio=$FECHA_INI&fechaFin=$FECHA_FIN"
assert "GET /transacciones/cuenta/{id}/filtro (cuenta ajena → 403)" "403" "$RESP_CODE" "$RESP_BODY"

# filtro sin auth
http "" "" GET "/api/v1/transacciones/cuenta/$BRYAN_CUENTA_ID/filtro?fechaInicio=$FECHA_INI&fechaFin=$FECHA_FIN"
assert "GET /transacciones/cuenta/{id}/filtro (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# filtro sin parámetros de fecha (fechaInicio/fechaFin obligatorios)
http "$COOKIE_BRYAN" "" GET "/api/v1/transacciones/cuenta/$BRYAN_CUENTA_ID/filtro"
assert "GET /transacciones/cuenta/{id}/filtro (sin params → 400)" "400" "$RESP_CODE" "$RESP_BODY"

# depositar correcto
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/depositar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"monto\":10000}"
assert "POST /transacciones/depositar (correcto → 200)" "200" "$RESP_CODE" "$RESP_BODY"
assert_contains "  respuesta tiene idTransaccion" "idTransaccion" "$RESP_BODY" "$RESP_CODE"

# depositar cuenta ajena
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/depositar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$ANA_CUENTA_ID,\"monto\":10000}"
assert "POST /transacciones/depositar (cuenta ajena → 403)" "403" "$RESP_CODE" "$RESP_BODY"

# depositar sin auth
http "" "" POST /api/v1/transacciones/depositar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"monto\":10000}"
assert "POST /transacciones/depositar (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# retirar correcto
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/retirar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"monto\":5000}"
assert "POST /transacciones/retirar (correcto → 200)" "200" "$RESP_CODE" "$RESP_BODY"

# retirar saldo insuficiente
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/retirar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"monto\":99999999}"
assert "POST /transacciones/retirar (saldo insuficiente → 409)" "409" "$RESP_CODE" "$RESP_BODY"

# retirar cuenta ajena
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/retirar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$ANA_CUENTA_ID,\"monto\":5000}"
assert "POST /transacciones/retirar (cuenta ajena → 403)" "403" "$RESP_CODE" "$RESP_BODY"

# retirar sin auth
http "" "" POST /api/v1/transacciones/retirar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"monto\":5000}"
assert "POST /transacciones/retirar (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# transferir correcto
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$BRYAN_CUENTA_ID,\"numeroCuentaDestino\":\"$ANA_CUENTA_NUM\",\"monto\":1000}"
assert "POST /transacciones/transferir (correcto → 200)" "200" "$RESP_CODE" "$RESP_BODY"

# transferir destino inexistente
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$BRYAN_CUENTA_ID,\"numeroCuentaDestino\":\"00000000\",\"monto\":1000}"
assert "POST /transacciones/transferir (destino inexistente → 404)" "404" "$RESP_CODE" "$RESP_BODY"

# transferir saldo insuficiente
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$BRYAN_CUENTA_ID,\"numeroCuentaDestino\":\"$ANA_CUENTA_NUM\",\"monto\":99999999}"
assert "POST /transacciones/transferir (saldo insuficiente → 409)" "409" "$RESP_CODE" "$RESP_BODY"

# transferir cuenta origen ajena
http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$ANA_CUENTA_ID,\"numeroCuentaDestino\":\"$BRYAN_CUENTA_NUM\",\"monto\":1000}"
assert "POST /transacciones/transferir (cuenta origen ajena → 403)" "403" "$RESP_CODE" "$RESP_BODY"

# transferir sin auth
http "" "" POST /api/v1/transacciones/transferir \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$BRYAN_CUENTA_ID,\"numeroCuentaDestino\":\"$ANA_CUENTA_NUM\",\"monto\":1000}"
assert "POST /transacciones/transferir (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# ── 7b. CUENTA BLOQUEADA ─────────────────────────────────────
section "7b. CUENTA BLOQUEADA"

# Bloquear la cuenta de bryan para probar operaciones sobre cuenta bloqueada
http "$COOKIE_BRYAN" "" POST /api/v1/cuentas/seguridad/bloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"bryan123"}'
assert "  setup: bloquear cuenta bryan → 200" "200" "$RESP_CODE" "$RESP_BODY"

http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/depositar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"monto\":10000}"
assert "POST /transacciones/depositar (cuenta bloqueada → 400)" "400" "$RESP_CODE" "$RESP_BODY"

http "$COOKIE_BRYAN" "" POST /api/v1/transacciones/retirar \
  -H "Content-Type: application/json" \
  -d "{\"idCuenta\":$BRYAN_CUENTA_ID,\"monto\":5000}"
assert "POST /transacciones/retirar (cuenta bloqueada → 400)" "400" "$RESP_CODE" "$RESP_BODY"

# Desbloquear para devolver al estado normal
http "$COOKIE_BRYAN" "" POST /api/v1/cuentas/seguridad/desbloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"bryan123"}'
assert "  teardown: desbloquear cuenta bryan → 200" "200" "$RESP_CODE" "$RESP_BODY"

# ── 8. MOTOR DE VALIDACIÓN ───────────────────────────────────
section "8. MOTOR DE VALIDACIÓN"

# validar correcto (saldo suficiente)
http "$COOKIE_BRYAN" "" POST /api/v1/validaciones/transaccion \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$BRYAN_CUENTA_ID,\"monto\":100,\"tipoOperacion\":\"RETIRO\"}"
assert "POST /validaciones/transaccion (autorizada → 200)" "200" "$RESP_CODE" "$RESP_BODY"
assert_contains "  respuesta tiene autorizada" "autorizada" "$RESP_BODY" "$RESP_CODE"

# validar saldo insuficiente (sigue retornando 200 pero no autorizada)
http "$COOKIE_BRYAN" "" POST /api/v1/validaciones/transaccion \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$BRYAN_CUENTA_ID,\"monto\":99999999,\"tipoOperacion\":\"RETIRO\"}"
assert "POST /validaciones/transaccion (no autorizada — saldo → 200)" "200" "$RESP_CODE" "$RESP_BODY"

# validar cuenta ajena (bryan intenta validar la cuenta de ana)
http "$COOKIE_BRYAN" "" POST /api/v1/validaciones/transaccion \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$ANA_CUENTA_ID,\"monto\":100,\"tipoOperacion\":\"RETIRO\"}"
assert "POST /validaciones/transaccion (cuenta ajena → 403)" "403" "$RESP_CODE" "$RESP_BODY"

# validar sin auth
http "" "" POST /api/v1/validaciones/transaccion \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$BRYAN_CUENTA_ID,\"monto\":100,\"tipoOperacion\":\"RETIRO\"}"
assert "POST /validaciones/transaccion (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# validar cuenta bloqueada (retorna 200 con autorizada=false y código CUENTA_BLOQUEADA)
http "$COOKIE_BRYAN" "" POST /api/v1/cuentas/seguridad/bloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"bryan123"}'
# (no se cuenta como test de validacion, es setup)

http "$COOKIE_BRYAN" "" POST /api/v1/validaciones/transaccion \
  -H "Content-Type: application/json" \
  -d "{\"idCuentaOrigen\":$BRYAN_CUENTA_ID,\"monto\":100,\"tipoOperacion\":\"RETIRO\"}"
assert "POST /validaciones/transaccion (cuenta bloqueada → 200 no autorizada)" "200" "$RESP_CODE" "$RESP_BODY"
assert_contains "  respuesta contiene CUENTA_BLOQUEADA" "CUENTA_BLOQUEADA" "$RESP_BODY" "$RESP_CODE"

http "$COOKIE_BRYAN" "" POST /api/v1/cuentas/seguridad/desbloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"bryan123"}'
# (no se cuenta, es teardown)

# ── 9. TRANSFERENCIAS INTERBANCARIAS (ACH) ───────────────────
section "9. TRANSFERENCIAS INTERBANCARIAS (ACH)"

# crear ACH correcto
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
assert "POST /transferencias/interbancarias (correcto → 200)" "200" "$RESP_CODE" "$RESP_BODY"
TX_ACH_ID=$(echo "$RESP_BODY" | grep -o '"idTransaccion":[0-9]*' | head -1 | cut -d: -f2)
echo "  → idTransaccion ACH confirmación: $TX_ACH_ID"

if [ -n "$TX_ACH_ID" ]; then
  # confirmacion-ach correcto
  http "" "" POST /api/v1/transferencias/interbancarias/$TX_ACH_ID/confirmacion-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"referenciaConfirmacion":"ACH-TEST-CONFIRM-001"}'
  assert "POST /confirmacion-ach (correcto → 200)" "200" "$RESP_CODE" "$RESP_BODY"

  # rechazo sobre ya confirmada
  http "" "" POST /api/v1/transferencias/interbancarias/$TX_ACH_ID/rechazo-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"motivo":"prueba doble operacion"}'
  assert "POST /rechazo-ach sobre ya confirmada → 400" "400" "$RESP_CODE" "$RESP_BODY"

  # confirmacion secret incorrecto
  http "" "" POST /api/v1/transferencias/interbancarias/$TX_ACH_ID/confirmacion-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: wrong_secret" \
    -d '{"referenciaConfirmacion":"ACH-BAD"}'
  assert "POST /confirmacion-ach (secret incorrecto → 403)" "403" "$RESP_CODE" "$RESP_BODY"

  # confirmacion sin header
  TX_NO_SEC_SETUP=$(echo "$RESP_BODY" | head -c 0)  # dummy
  http "$COOKIE_BRYAN" "" POST /api/v1/transferencias/interbancarias \
    -H "Content-Type: application/json" \
    -d "{
      \"idCuentaOrigen\":$BRYAN_CUENTA_ID,
      \"bancoDestino\":\"Banco\",\"numeroCuentaDestino\":\"111\",
      \"tipoCuentaDestino\":\"AHORROS\",\"tipoDocumentoReceptor\":\"CC\",
      \"numeroDocumentoReceptor\":\"111\",\"nombreReceptor\":\"R\",\"monto\":5000
    }"
  TX_NO_SECRET=$(echo "$RESP_BODY" | grep -o '"idTransaccion":[0-9]*' | head -1 | cut -d: -f2)
  if [ -n "$TX_NO_SECRET" ]; then
    http "" "" POST /api/v1/transferencias/interbancarias/$TX_NO_SECRET/confirmacion-ach \
      -H "Content-Type: application/json" \
      -d '{"referenciaConfirmacion":"ACH-NO-SECRET"}'
    assert "POST /confirmacion-ach (sin header X-Gateway-Secret → 403)" "403" "$RESP_CODE" "$RESP_BODY"
  fi

  # consultar propia
  http "$COOKIE_BRYAN" "" GET /api/v1/transferencias/interbancarias/$TX_ACH_ID
  assert "GET /transferencias/interbancarias/{id} (propia → 200)" "200" "$RESP_CODE" "$RESP_BODY"

  # consultar ajena
  http "$COOKIE_ANA" "" GET /api/v1/transferencias/interbancarias/$TX_ACH_ID
  assert "GET /transferencias/interbancarias/{id} (ajena → 403)" "403" "$RESP_CODE" "$RESP_BODY"

  # consultar sin auth
  http "" "" GET /api/v1/transferencias/interbancarias/$TX_ACH_ID
  assert "GET /transferencias/interbancarias/{id} (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

  # confirmacion id inexistente
  http "" "" POST /api/v1/transferencias/interbancarias/99999/confirmacion-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"referenciaConfirmacion":"ACH-NOT-FOUND"}'
  assert "POST /confirmacion-ach (id inexistente → 404)" "404" "$RESP_CODE" "$RESP_BODY"

  # rechazo id inexistente
  http "" "" POST /api/v1/transferencias/interbancarias/99999/rechazo-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"motivo":"no existe"}'
  assert "POST /rechazo-ach (id inexistente → 404)" "404" "$RESP_CODE" "$RESP_BODY"
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
assert "POST /transferencias/interbancarias (para flujo rechazo → 200)" "200" "$RESP_CODE" "$RESP_BODY"
TX_ACH_REJ_ID=$(echo "$RESP_BODY" | grep -o '"idTransaccion":[0-9]*' | head -1 | cut -d: -f2)

if [ -n "$TX_ACH_REJ_ID" ]; then
  http "" "" POST /api/v1/transferencias/interbancarias/$TX_ACH_REJ_ID/rechazo-ach \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"motivo":"Cuenta destino invalida ACH_01"}'
  assert "POST /rechazo-ach (reversión automática → 200)" "200" "$RESP_CODE" "$RESP_BODY"
fi

# crear ACH sin auth
http "" "" POST /api/v1/transferencias/interbancarias \
  -H "Content-Type: application/json" \
  -d "{
    \"idCuentaOrigen\":$BRYAN_CUENTA_ID,
    \"bancoDestino\":\"Bancolombia\",\"numeroCuentaDestino\":\"123456789\",
    \"tipoCuentaDestino\":\"AHORROS\",\"tipoDocumentoReceptor\":\"CC\",
    \"numeroDocumentoReceptor\":\"1122334455\",\"nombreReceptor\":\"Test\",\"monto\":1000
  }"
assert "POST /transferencias/interbancarias (sin auth → 401)" "401" "$RESP_CODE" "$RESP_BODY"

# crear ACH saldo insuficiente
http "$COOKIE_BRYAN" "" POST /api/v1/transferencias/interbancarias \
  -H "Content-Type: application/json" \
  -d "{
    \"idCuentaOrigen\":$BRYAN_CUENTA_ID,
    \"bancoDestino\":\"Bancolombia\",\"numeroCuentaDestino\":\"123456789\",
    \"tipoCuentaDestino\":\"AHORROS\",\"tipoDocumentoReceptor\":\"CC\",
    \"numeroDocumentoReceptor\":\"1122334455\",\"nombreReceptor\":\"Test\",\"monto\":99999999
  }"
assert "POST /transferencias/interbancarias (saldo insuficiente → 409)" "409" "$RESP_CODE" "$RESP_BODY"

# crear ACH cuenta bloqueada
http "$COOKIE_BRYAN" "" POST /api/v1/cuentas/seguridad/bloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"bryan123"}'

http "$COOKIE_BRYAN" "" POST /api/v1/transferencias/interbancarias \
  -H "Content-Type: application/json" \
  -d "{
    \"idCuentaOrigen\":$BRYAN_CUENTA_ID,
    \"bancoDestino\":\"Bancolombia\",\"numeroCuentaDestino\":\"123456789\",
    \"tipoCuentaDestino\":\"AHORROS\",\"tipoDocumentoReceptor\":\"CC\",
    \"numeroDocumentoReceptor\":\"1122334455\",\"nombreReceptor\":\"Test\",\"monto\":1000
  }"
assert "POST /transferencias/interbancarias (cuenta bloqueada → 400)" "400" "$RESP_CODE" "$RESP_BODY"

http "$COOKIE_BRYAN" "" POST /api/v1/cuentas/seguridad/desbloquear \
  -H "Content-Type: application/json" \
  -d '{"password":"bryan123"}'

# consultar id inexistente autenticado
http "$COOKIE_BRYAN" "" GET /api/v1/transferencias/interbancarias/99999
assert "GET /transferencias/interbancarias/{id} (id inexistente → 404)" "404" "$RESP_CODE" "$RESP_BODY"

# ── RESUMEN ──────────────────────────────────────────────────
echo ""
echo "══════════════════════════════════════════"
echo " RESULTADO: $PASS pasaron | $FAIL fallaron | $WARN advertencias"
echo "══════════════════════════════════════════"

# Cleanup
rm -f "$COOKIE_BRYAN" "$COOKIE_ANA" "$COOKIE_TEMP" /tmp/resp_body.txt

[ "$FAIL" -eq 0 ] && exit 0 || exit 1
