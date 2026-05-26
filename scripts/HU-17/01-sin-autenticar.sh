#!/bin/bash
# HU-17 · Seguridad — Sin autenticar
#
# Valida que todos los endpoints de reportes devuelvan 401
# cuando se accede sin sesión activa.

source "$(dirname "$0")/_comun.sh"

echo "========================================================"
echo "  HU-17 · Seguridad — Sin autenticar"
echo "========================================================"

for endpoint in "consolidado" "estado?estado=ACTIVA" "rango?min=0" "tiempo-real"; do
  RUTA="${endpoint%%\?*}"
  PARAMS="${endpoint#*\?}"; [ "$PARAMS" = "$endpoint" ] && PARAMS=""
  echo ""
  echo "--- GET /reportes/saldos/${RUTA} sin sesión ---"
  RESP=$(get_reporte_sin_auth "$RUTA" "$PARAMS")
  HTTP=$(echo "$RESP" | tail -1)
  echo "  HTTP: $HTTP"
  [ "$HTTP" = "401" ] \
    && ok "Sin sesión → 401 Unauthorized (/${RUTA})" \
    || fail "Esperado 401, obtenido $HTTP (/${RUTA})"
done

resumen
