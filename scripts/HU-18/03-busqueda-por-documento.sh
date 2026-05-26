#!/bin/bash
# HU-18 · Escenario 1 — Búsqueda por número de documento
#
# Valida:
#   - HTTP 200 con rol ADMIN
#   - Respuesta contiene nombre, estadoCuenta, fechaVinculacion del cliente
#   - El campo documento está enmascarado (formato ***XXX)
#   - El campo numeroCuenta está presente
#   - totalMovimientos presente

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-18 · Escenario 1 — Búsqueda por documento"
echo "========================================================"

echo "--- Login como bryan (rol ADMIN) ---"
login "bryan" "bryan123" "$COOKIES_ADMIN"

echo ""
echo "--- GET /buscar/documento?documento=$DOC_ANA (Ana Gómez) ---"
RESP=$(buscar_doc "$DOC_ANA" "" "" "" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)
echo "$BODY" | jq . 2>/dev/null
echo ""

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK" \
  || fail "Esperado 200, obtenido: $HTTP"

NOMBRE=$(echo "$BODY" | jq -r '.cliente.nombre // empty')
[ "$NOMBRE" = "Ana Gómez" ] \
  && ok "cliente.nombre correcto: $NOMBRE" \
  || fail "cliente.nombre incorrecto, obtenido: '$NOMBRE'"

ESTADO=$(echo "$BODY" | jq -r '.cliente.estadoCuenta // empty')
[ -n "$ESTADO" ] && [ "$ESTADO" != "null" ] \
  && ok "cliente.estadoCuenta presente: $ESTADO" \
  || fail "cliente.estadoCuenta ausente"

FECHA=$(echo "$BODY" | jq -r '.cliente.fechaVinculacion // empty')
[ -n "$FECHA" ] && [ "$FECHA" != "null" ] \
  && ok "cliente.fechaVinculacion presente: $FECHA" \
  || fail "cliente.fechaVinculacion ausente"

# documento no debe ser el número real (está enmascarado para ADMIN)
DOC_RESP=$(echo "$BODY" | jq -r '.cliente.documento // empty')
[ "$DOC_RESP" != "$DOC_ANA" ] \
  && ok "documento enmascarado (no expone número real): $DOC_RESP" \
  || fail "documento NO está enmascarado, expone: $DOC_RESP"

echo "$DOC_RESP" | grep -q "^\*\*\*" \
  && ok "documento con prefijo *** correcto: $DOC_RESP" \
  || fail "formato de enmascarado incorrecto: $DOC_RESP"

NUM_CTA=$(echo "$BODY" | jq -r '.cliente.numeroCuenta // empty')
[ -n "$NUM_CTA" ] && [ "$NUM_CTA" != "null" ] \
  && ok "cliente.numeroCuenta presente: $NUM_CTA" \
  || fail "cliente.numeroCuenta ausente"

TOTAL=$(echo "$BODY" | jq -r '.totalMovimientos // empty')
[ -n "$TOTAL" ] && [ "$TOTAL" != "null" ] \
  && ok "totalMovimientos presente: $TOTAL" \
  || fail "totalMovimientos ausente"

rm -f "$COOKIES_ADMIN"
resumen
