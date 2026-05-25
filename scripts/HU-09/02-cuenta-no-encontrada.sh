#!/bin/bash
# Escenario 2: idCuenta que no existe o no pertenece al usuario autenticado
# Esperado: 403 Forbidden (AccesoNoAutorizadoException)

source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 2: cuenta no encontrada ==="
login "bryan" "bryan123"

http_code=$(depositar 99999 50000)

[ "$http_code" = "403" ] \
  && ok "HTTP 403 Forbidden (cuenta no accesible para este usuario)" \
  || fail "Esperado 403, obtenido: $http_code"

resumen
