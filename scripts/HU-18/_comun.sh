#!/bin/bash
# Helpers compartidos para los tests de HU-18 — Búsqueda Avanzada de Actividad por Cliente
#
# Datos de seed (perfil "seed"):
#   bryan / bryan123  → ADMIN   → doc 123456789 → cuentas 00010001 y 00010002
#   ana   / ana123    → CLIENTE → doc 987654321 → cuenta  00020001
#   carlos/ carlos123 → CLIENTE → doc 111111111 → cuenta  00030001
#
# NOTA — GERENTE: el rol existe en BD pero ningún usuario seed lo tiene asignado.
#   Para probar el enmascarado de GERENTE se requiere crear un usuario manualmente
#   (no hay endpoint de registro con rol GERENTE en el API).

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

# buscar_doc <documento> <fechaInicio|""> <fechaFin|""> <tipo|""> <jar>
buscar_doc() {
  local doc="$1" inicio="$2" fin="$3" tipo="$4" jar="$5"
  local url="${URL_BASE}/admin/clientes/buscar/documento?documento=${doc}"
  [ -n "$inicio" ] && url="${url}&fechaInicio=${inicio}"
  [ -n "$fin"    ] && url="${url}&fechaFin=${fin}"
  [ -n "$tipo"   ] && url="${url}&tipo=${tipo}"
  curl -s -w "\n%{http_code}" -b "$jar" "$url"
}

buscar_doc_sin_auth() {
  curl -s -w "\n%{http_code}" \
    "${URL_BASE}/admin/clientes/buscar/documento?documento=${1}"
}

# buscar_cuenta <numeroCuenta> <fechaInicio|""> <fechaFin|""> <tipo|""> <jar>
buscar_cuenta() {
  local num="$1" inicio="$2" fin="$3" tipo="$4" jar="$5"
  local url="${URL_BASE}/admin/clientes/buscar/cuenta?numeroCuenta=${num}"
  [ -n "$inicio" ] && url="${url}&fechaInicio=${inicio}"
  [ -n "$fin"    ] && url="${url}&fechaFin=${fin}"
  [ -n "$tipo"   ] && url="${url}&tipo=${tipo}"
  curl -s -w "\n%{http_code}" -b "$jar" "$url"
}

buscar_cuenta_sin_auth() {
  curl -s -w "\n%{http_code}" \
    "${URL_BASE}/admin/clientes/buscar/cuenta?numeroCuenta=${1}"
}

# Documentos y cuentas del seed
DOC_BRYAN="123456789"
DOC_ANA="987654321"
CTA_BRYAN="00010001"
CTA_BRYAN2="00010002"
CTA_ANA="00020001"
