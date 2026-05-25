#!/bin/bash
# Escenario 0: verificar setup — login y obtener idCuenta de bryan
# Este script confirma que la autenticación y el dashboard funcionan
# (reemplaza el antiguo flujo de generación de token OTP)

source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 0: verificar setup ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)
SALDO=$(get_saldo)

echo "  idCuenta : $ID"
echo "  saldo    : $SALDO"

[ -n "$ID" ] && ok "Login y dashboard OK (idCuenta=$ID)" || fail "No se pudo obtener idCuenta"

resumen
