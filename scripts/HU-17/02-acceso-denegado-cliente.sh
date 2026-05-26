#!/bin/bash
# HU-17 · Seguridad — Rol CLIENTE denegado
#
# Valida que un usuario con rol CLIENTE reciba 403 en todos
# los endpoints de reportes (requieren ADMIN o GERENTE).

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-17 · Seguridad — Acceso denegado (rol CLIENTE)"
echo "========================================================"

echo "--- Login como ana (rol CLIENTE) ---"
login "ana" "ana123" "$COOKIES_CLIENTE"

for endpoint in "consolidado" "estado?estado=ACTIVA" "rango?min=0" "tiempo-real"; do
  RUTA="${endpoint%%\?*}"
  PARAMS="${endpoint#*\?}"; [ "$PARAMS" = "$endpoint" ] && PARAMS=""
  echo ""
  echo "--- GET /reportes/saldos/${RUTA} con rol CLIENTE ---"
  RESP=$(get_reporte "$RUTA" "$PARAMS" "$COOKIES_CLIENTE")
  HTTP=$(echo "$RESP" | tail -1)
  echo "  HTTP: $HTTP"
  [ "$HTTP" = "403" ] \
    && ok "CLIENTE → 403 Forbidden (/${RUTA})" \
    || fail "Esperado 403, obtenido $HTTP (/${RUTA})"
done

rm -f "$COOKIES_CLIENTE"
resumen
