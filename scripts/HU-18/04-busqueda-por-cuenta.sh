#!/bin/bash
# HU-18 · Escenario 1 — Búsqueda por número de cuenta
#
# Valida:
#   - HTTP 200 al buscar por numeroCuenta con rol ADMIN
#   - Devuelve los mismos datos básicos del cliente que la búsqueda por documento
#   - estadoCuenta = ACTIVA para cuenta 00020001 (Ana)
#   - numeroCuenta enmascarado para ADMIN

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-18 · Escenario 1 — Búsqueda por número de cuenta"
echo "========================================================"

echo "--- Login como bryan (rol ADMIN) ---"
login "bryan" "bryan123" "$COOKIES_ADMIN"

echo ""
echo "--- GET /buscar/cuenta?numeroCuenta=$CTA_ANA (cuenta de Ana) ---"
RESP=$(buscar_cuenta "$CTA_ANA" "" "" "" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)
echo "$BODY" | jq . 2>/dev/null
echo ""

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK" \
  || fail "Esperado 200, obtenido: $HTTP"

NOMBRE=$(echo "$BODY" | jq -r '.cliente.nombre // empty')
[ "$NOMBRE" = "Ana Gómez" ] \
  && ok "cliente.nombre correcto: $NOMBRE (misma cliente que búsqueda por documento)" \
  || fail "cliente.nombre incorrecto, obtenido: '$NOMBRE'"

ESTADO=$(echo "$BODY" | jq -r '.cliente.estadoCuenta // empty')
[ "$ESTADO" = "ACTIVA" ] \
  && ok "estadoCuenta = ACTIVA" \
  || fail "estadoCuenta incorrecto: $ESTADO"

# numeroCuenta debe estar enmascarado para ADMIN (no exponer número real)
NUM_RESP=$(echo "$BODY" | jq -r '.cliente.numeroCuenta // empty')
[ "$NUM_RESP" != "$CTA_ANA" ] \
  && ok "numeroCuenta enmascarado (no expone $CTA_ANA): $NUM_RESP" \
  || fail "numeroCuenta NO está enmascarado"

echo "$NUM_RESP" | grep -q "^\*" \
  && ok "numeroCuenta con prefijo * correcto: $NUM_RESP" \
  || fail "formato de enmascarado de cuenta incorrecto: $NUM_RESP"

rm -f "$COOKIES_ADMIN"
resumen
