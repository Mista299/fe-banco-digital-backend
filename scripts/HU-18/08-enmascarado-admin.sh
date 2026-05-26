#!/bin/bash
# HU-18 · Escenario 4 — Enmascarado de datos sensibles (rol ADMIN)
#
# Valida:
#   - ADMIN ve documento con solo los últimos 3 dígitos (***321 para doc 987654321)
#   - ADMIN ve numeroCuenta con solo los últimos 4 dígitos (******0001 para 00020001)
#   - El nombre del cliente NO está enmascarado (es público en el contexto admin)
#
# Nota: para verificar que GERENTE ve datos sin enmascarar se requiere un usuario
#       con ese rol. El seed no incluye ninguno — crearlo manualmente si se necesita.

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-18 · Escenario 4 — Enmascarado de datos (ADMIN)"
echo "========================================================"

# Buscamos a Ana: doc real=987654321, cuenta real=00020001
# Esperado ADMIN: documento=***321, numeroCuenta=******0001

echo "--- Login como bryan (rol ADMIN) ---"
login "bryan" "bryan123" "$COOKIES_ADMIN"

echo ""
echo "--- GET /buscar/documento?documento=$DOC_ANA ---"
RESP=$(buscar_doc "$DOC_ANA" "" "" "" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(echo "$RESP" | head -n -1)
echo ""

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK" \
  || fail "Esperado 200, obtenido: $HTTP"

# ── Verificar documento enmascarado ──────────────────────────────────────────
DOC_RESP=$(echo "$BODY" | jq -r '.cliente.documento // empty')
echo "  documento en respuesta : $DOC_RESP"
echo "  documento real          : $DOC_ANA"

[ "$DOC_RESP" != "$DOC_ANA" ] \
  && ok "documento ≠ número real (enmascarado)" \
  || fail "documento expuesto sin enmascarar: $DOC_RESP"

echo "$DOC_RESP" | grep -q "^\*\*\*" \
  && ok "documento inicia con ***: $DOC_RESP" \
  || fail "documento no inicia con ***: $DOC_RESP"

# Últimos 3 dígitos de 987654321 → "321"
echo "$DOC_RESP" | grep -q "321$" \
  && ok "documento termina con los últimos 3 dígitos correctos ('321'): $DOC_RESP" \
  || fail "documento no termina con '321': $DOC_RESP"

# ── Verificar numeroCuenta enmascarado ────────────────────────────────────────
NUM_RESP=$(echo "$BODY" | jq -r '.cliente.numeroCuenta // empty')
echo ""
echo "  numeroCuenta en respuesta : $NUM_RESP"
echo "  numeroCuenta real          : $CTA_ANA"

[ "$NUM_RESP" != "$CTA_ANA" ] \
  && ok "numeroCuenta ≠ número real (enmascarado)" \
  || fail "numeroCuenta expuesto sin enmascarar: $NUM_RESP"

echo "$NUM_RESP" | grep -q "^\*\*\*\*\*\*" \
  && ok "numeroCuenta inicia con ******: $NUM_RESP" \
  || fail "numeroCuenta no inicia con ******: $NUM_RESP"

# Últimos 4 dígitos de 00020001 → "0001"
echo "$NUM_RESP" | grep -q "0001$" \
  && ok "numeroCuenta termina con los últimos 4 dígitos correctos ('0001'): $NUM_RESP" \
  || fail "numeroCuenta no termina con '0001': $NUM_RESP"

# ── Nombre NO debe estar enmascarado ─────────────────────────────────────────
NOMBRE=$(echo "$BODY" | jq -r '.cliente.nombre // empty')
[ "$NOMBRE" = "Ana Gómez" ] \
  && ok "nombre no enmascarado (visible para ADMIN): $NOMBRE" \
  || fail "nombre incorrecto o ausente: '$NOMBRE'"

rm -f "$COOKIES_ADMIN"
resumen
