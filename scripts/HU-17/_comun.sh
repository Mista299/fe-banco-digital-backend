#!/bin/bash
# Helpers compartidos para los tests de HU-17 — Reporte de Saldos Consolidados
#
# Datos de seed:
#   bryan / bryan123 → ADMIN   (puede acceder a reportes)
#   ana   / ana123   → CLIENTE (debe recibir 403)
#
# Cuentas seed:
#   00010001 Bryan  AHORROS   ACTIVA    550000
#   00020001 Ana    AHORROS   ACTIVA    745000
#   00030001 Carlos AHORROS   ACTIVA    800000
#   00040001 Laura  CORRIENTE ACTIVA    100000
#   00050001 Jorge  AHORROS   INACTIVA  0
#   00060001 Sofía  AHORROS   ACTIVA    620000
#   00010002 Bryan  CORRIENTE ACTIVA   1350000
#   00030002 Carlos CORRIENTE ACTIVA    540000

source "$(dirname "${BASH_SOURCE[0]}")/../config.sh"
URL_BASE="${API_BASE}/api/v1"
COOKIES_ADMIN="$(dirname "${BASH_SOURCE[0]}")/cookies_admin.txt"
COOKIES_CLIENTE="$(dirname "${BASH_SOURCE[0]}")/cookies_cliente.txt"
PASS=0
FAIL=0

ok()   { echo "  [PASS] $1"; PASS=$((PASS + 1)); }
fail() { echo "  [FAIL] $1"; FAIL=$((FAIL + 1)); }

resumen() {
  echo ""
  echo "========================================================"
  printf "  Resultado: %d pasaron · %d fallaron\n" "$PASS" "$FAIL"
  echo "========================================================"
  [ "$FAIL" -eq 0 ] && exit 0 || exit 1
}

login() {
  local user="$1" pass="$2" jar="$3"
  rm -f "$jar"
  curl -s -c "$jar" -X POST "${URL_BASE}/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"${user}\",\"password\":\"${pass}\"}" > /dev/null
}

# Llama GET /reportes/saldos/<ruta> con parámetros opcionales
# Devuelve BODY\nHTTP_CODE
get_reporte() {
  local ruta="$1" params="$2" jar="$3"
  local url="${URL_BASE}/reportes/saldos/${ruta}"
  [ -n "$params" ] && url="${url}?${params}"
  curl -s -w "\n%{http_code}" -b "$jar" "$url"
}

# Igual pero sin cookies (sin autenticar)
get_reporte_sin_auth() {
  local ruta="$1" params="$2"
  local url="${URL_BASE}/reportes/saldos/${ruta}"
  [ -n "$params" ] && url="${url}?${params}"
  curl -s -w "\n%{http_code}" "$url"
}
