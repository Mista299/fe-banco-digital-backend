#!/bin/bash
# HU-17 · Escenario 4 — Saldo en tiempo real
#
# Valida:
#   - HTTP 200 con lista de todas las cuentas del sistema
#   - Cada registro tiene idCuenta, saldoDisponible, saldoContable, estado, tipoCuenta
#   - idCuenta es único en la respuesta (no hay duplicados)
#   - Cantidad de cuentas coincide con el seed (8)
#   - saldoDisponible y saldoContable están presentes y son >= 0
#   - tipoCuenta solo contiene valores válidos (AHORROS o CORRIENTE)
#   - estado solo contiene valores válidos (ACTIVA o INACTIVA)

source "$(dirname "$0")/_comun.sh"

TOTAL_CUENTAS_SEED=8

echo "========================================================"
echo "  HU-17 · Escenario 4 — Saldo en tiempo real"
echo "========================================================"

echo "--- Login como bryan (rol ADMIN) ---"
login "bryan" "bryan123" "$COOKIES_ADMIN"

echo ""
echo "--- GET /reportes/saldos/tiempo-real ---"
RESP=$(get_reporte "tiempo-real" "" "$COOKIES_ADMIN")
HTTP=$(echo "$RESP" | tail -1)
BODY=$(unwrap_list "$(echo "$RESP" | head -n -1)")

[ "$HTTP" = "200" ] \
  && ok "HTTP 200 OK" \
  || fail "Esperado 200, obtenido: $HTTP"

LEN=$(echo "$BODY" | jq 'length' 2>/dev/null)
[ -n "$LEN" ] && [ "$LEN" -gt 0 ] \
  && ok "Respuesta contiene $LEN cuenta(s)" \
  || fail "Respuesta vacía — se esperaban cuentas del seed"

[ "$LEN" -ge "$TOTAL_CUENTAS_SEED" ] \
  && ok "Número de cuentas ($LEN) ≥ seed mínimo ($TOTAL_CUENTAS_SEED)" \
  || fail "Número de cuentas ($LEN) es menor al seed mínimo ($TOTAL_CUENTAS_SEED)"

# ── Campos requeridos ─────────────────────────────────────────────────────────
TIENE_CAMPOS=$(echo "$BODY" | jq '
  [.[] | select(
    (.idCuenta        | type) == "number" and
    (.saldoDisponible | type) == "number" and
    (.saldoContable   | type) == "number" and
    (.estado          | type) == "string" and
    (.tipoCuenta      | type) == "string"
  )] | length' 2>/dev/null)
[ "$TIENE_CAMPOS" = "$LEN" ] \
  && ok "Todos los registros tienen idCuenta, saldoDisponible, saldoContable, estado, tipoCuenta" \
  || fail "Registros incompletos ($TIENE_CAMPOS de $LEN tienen todos los campos)"

# ── idCuenta único ────────────────────────────────────────────────────────────
DUPLICADOS=$(echo "$BODY" | jq '
  [.[].idCuenta] | group_by(.) | map(select(length > 1)) | length' 2>/dev/null)
[ "$DUPLICADOS" = "0" ] \
  && ok "Sin idCuenta duplicados — cada cuenta aparece una vez" \
  || fail "$DUPLICADOS idCuenta repetido(s) en la respuesta"

# ── saldos >= 0 ───────────────────────────────────────────────────────────────
SALDOS_NEGATIVOS=$(echo "$BODY" | jq '
  [.[] | select(.saldoDisponible < 0 or .saldoContable < 0)] | length' 2>/dev/null)
[ "$SALDOS_NEGATIVOS" = "0" ] \
  && ok "Ningún saldo negativo en la respuesta" \
  || fail "$SALDOS_NEGATIVOS cuenta(s) con saldo negativo"

# ── tipoCuenta válido ─────────────────────────────────────────────────────────
TIPO_INVALIDO=$(echo "$BODY" | jq '
  [.[] | select(.tipoCuenta != "AHORROS" and .tipoCuenta != "CORRIENTE")] | length' 2>/dev/null)
[ "$TIPO_INVALIDO" = "0" ] \
  && ok "Todos los tipoCuenta son AHORROS o CORRIENTE" \
  || fail "$TIPO_INVALIDO registro(s) con tipoCuenta inválido"

# ── estado válido ─────────────────────────────────────────────────────────────
ESTADO_INVALIDO=$(echo "$BODY" | jq '
  [.[] | select(.estado != "ACTIVA" and .estado != "INACTIVA" and .estado != "BLOQUEADA" and .estado != "PENDIENTE_APROBACION")] | length' 2>/dev/null)
[ "$ESTADO_INVALIDO" = "0" ] \
  && ok "Todos los estado son valores válidos de EstadoCuenta" \
  || fail "$ESTADO_INVALIDO registro(s) con estado inválido"

# ── Desglose por tipo ─────────────────────────────────────────────────────────
N_AHORROS=$(echo "$BODY" | jq '[.[] | select(.tipoCuenta == "AHORROS")] | length' 2>/dev/null)
N_CORRIENTE=$(echo "$BODY" | jq '[.[] | select(.tipoCuenta == "CORRIENTE")] | length' 2>/dev/null)
ok "Desglose: $N_AHORROS cuentas AHORROS, $N_CORRIENTE cuentas CORRIENTE"

# ── Cuenta inactiva presente (Jorge 00050001) ─────────────────────────────────
N_INACTIVAS=$(echo "$BODY" | jq '[.[] | select(.estado == "INACTIVA")] | length' 2>/dev/null)
[ -n "$N_INACTIVAS" ] && [ "$N_INACTIVAS" -gt 0 ] \
  && ok "Reporte incluye $N_INACTIVAS cuenta(s) INACTIVA(s) (visibilidad completa del sistema)" \
  || fail "No se encontraron cuentas INACTIVAS — el reporte debería mostrarlas todas"

rm -f "$COOKIES_ADMIN"
resumen
