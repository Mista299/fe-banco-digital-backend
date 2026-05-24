#!/bin/bash
# Escenario 4: cuenta de otro usuario → 403
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 4: cuenta no pertenece al usuario ==="
login "ana" "ana123"
ID=$(get_id_cuenta)   # id de la cuenta de ana

# Ahora logueamos como bryan e intentamos acceder a la cuenta de ana
login "bryan" "bryan123"

http_code=$(curl -s -o /dev/null -w "%{http_code}" \
  -b "$COOKIES" "${URL_BASE}/extractos/$ID/2026/4")

[ "$http_code" = "403" ] && ok "403 Forbidden correcto" \
                           || fail "Esperado 403, obtenido: $http_code"

resumen
