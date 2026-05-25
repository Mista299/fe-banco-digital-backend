#!/bin/bash
# Verifica el historial de transferencias interbancarias de bryan
# Uso: ./00-verificar-transferencias.sh [id_transaccion]

source "$(dirname "$0")/_comun.sh"

echo "================================================"
echo "  Verificador de transferencias interbancarias  "
echo "================================================"

login "bryan" "bryan123"
ID=$(get_id_cuenta)

echo ""
echo "Historial de movimientos — cuenta $ID (bryan):"
curl -s -b "$COOKIES" "${URL_BASE}/transacciones/cuenta/$ID" \
  | python3 -m json.tool 2>/dev/null || echo "(sin movimientos o error)"

if [ -n "$1" ]; then
  echo ""
  echo "Detalle de transferencia ID $1:"
  curl -s -b "$COOKIES" "${TRANSFER_URL}/$1" \
    | python3 -m json.tool 2>/dev/null || echo "(no encontrada)"
fi

echo ""
echo "Verificacion completada"
rm -f "$COOKIES"
