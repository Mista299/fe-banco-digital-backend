#!/bin/bash
# HU-18 · Consolidación multi-cuenta y deduplicación
#
# Bryan (c1) tiene DOS cuentas: 00010001 y 00010002.
# Hay transferencias internas entre ellas.
#
# Valida:
#   - La búsqueda por documento consolida movimientos de AMBAS cuentas
#   - No hay idTransaccion duplicados (transferencias internas no se repiten)
#   - totalMovimientos no está inflado por duplicados
#   - La búsqueda por cuenta (una sola) devuelve ≤ movimientos que por documento

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-18 · Consolidación — Cliente con 2 cuentas (Bryan)"
echo "========================================================"

echo "--- Login como bryan (rol ADMIN) ---"
login "bryan" "bryan123" "$COOKIES_ADMIN"

# ── Búsqueda por documento: consolida las 2 cuentas de Bryan ─────────────────
echo ""
echo "--- GET /buscar/documento?documento=$DOC_BRYAN (Bryan, cuentas $CTA_BRYAN y $CTA_BRYAN2) ---"
RESP_DOC=$(buscar_doc "$DOC_BRYAN" "" "" "" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP_DOC" | tail -1)
BODY_DOC=$(echo "$RESP_DOC" | head -n -1)

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK (cliente con 2 cuentas)" \
  || fail "Esperado 200, obtenido: $HTTP"

LEN_DOC=$(echo "$BODY_DOC" | jq '.movimientos | length' 2>/dev/null)
[ -n "$LEN_DOC" ] && [ "$LEN_DOC" -gt 0 ] \
  && ok "Historial consolidado tiene $LEN_DOC movimientos" \
  || fail "No se obtuvieron movimientos para Bryan"

TOTAL_DOC=$(echo "$BODY_DOC" | jq -r '.totalMovimientos // 0')
MATCH=$(python3 -c "print($TOTAL_DOC == $LEN_DOC)" 2>/dev/null)
[ "$MATCH" = "True" ] \
  && ok "totalMovimientos ($TOTAL_DOC) coincide con el array ($LEN_DOC)" \
  || fail "Discrepancia: totalMovimientos=$TOTAL_DOC pero array tiene $LEN_DOC"

# ── Sin idTransaccion duplicados ──────────────────────────────────────────────
DUPLICADOS=$(echo "$BODY_DOC" | jq '
  [.movimientos[].idTransaccion] |
  group_by(.) |
  map(select(length > 1)) |
  length' 2>/dev/null)
[ "$DUPLICADOS" = "0" ] \
  && ok "Sin idTransaccion duplicados (transferencias internas no se repiten)" \
  || fail "$DUPLICADOS idTransaccion repetido(s): transferencias internas duplicadas"

# ── Búsqueda por cuenta única: debe tener ≤ movimientos que el consolidado ───
echo ""
echo "--- GET /buscar/cuenta?numeroCuenta=$CTA_BRYAN (solo cuenta principal) ---"
RESP_CTA=$(buscar_cuenta "$CTA_BRYAN" "" "" "" "$COOKIES_ADMIN")
HTTP2=$(echo "$RESP_CTA" | tail -1)
BODY_CTA=$(echo "$RESP_CTA" | head -n -1)

[ "$HTTP2" = "200" ] \
  && ok "HTTP 200 OK (búsqueda por cuenta única $CTA_BRYAN)" \
  || fail "Esperado 200, obtenido: $HTTP2"

LEN_CTA=$(echo "$BODY_CTA" | jq '.movimientos | length' 2>/dev/null)
MENOS=$(python3 -c "print($LEN_CTA <= $LEN_DOC)" 2>/dev/null)
[ "$MENOS" = "True" ] \
  && ok "Cuenta única ($LEN_CTA mov) ≤ consolidado por documento ($LEN_DOC mov)" \
  || fail "Cuenta única ($LEN_CTA) > consolidado ($LEN_DOC): algo está mal"

# ── La segunda cuenta también aporta movimientos ─────────────────────────────
echo ""
echo "--- GET /buscar/cuenta?numeroCuenta=$CTA_BRYAN2 (segunda cuenta de Bryan) ---"
RESP_CTA2=$(buscar_cuenta "$CTA_BRYAN2" "" "" "" "$COOKIES_ADMIN")
HTTP3=$(echo "$RESP_CTA2" | tail -1)
BODY_CTA2=$(echo "$RESP_CTA2" | head -n -1)

[ "$HTTP3" = "200" ] \
  && ok "HTTP 200 OK (segunda cuenta $CTA_BRYAN2)" \
  || fail "Esperado 200 para segunda cuenta, obtenido: $HTTP3"

LEN_CTA2=$(echo "$BODY_CTA2" | jq '.movimientos | length' 2>/dev/null)
[ -n "$LEN_CTA2" ] && [ "$LEN_CTA2" -gt 0 ] \
  && ok "Segunda cuenta aporta $LEN_CTA2 movimiento(s) adicionales" \
  || fail "Segunda cuenta no tiene movimientos (esperado >0 por seed)"

# El consolidado debe tener más movimientos que cualquiera de las dos cuentas por separado
MAS=$(python3 -c "print($LEN_DOC >= $LEN_CTA and $LEN_DOC >= $LEN_CTA2)" 2>/dev/null)
[ "$MAS" = "True" ] \
  && ok "Consolidado ($LEN_DOC) ≥ cuenta 1 ($LEN_CTA) y cuenta 2 ($LEN_CTA2)" \
  || fail "Consolidado ($LEN_DOC) debería ser ≥ ambas cuentas individuales"

rm -f "$COOKIES_ADMIN"
resumen
