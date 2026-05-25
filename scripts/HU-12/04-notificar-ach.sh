#!/bin/bash
# Utilidad: enviar confirmación o rechazo ACH para una transacción existente
# Uso: ./04-notificar-ach.sh <id_transaccion> <confirmacion|rechazo>
# Ejemplo: ./04-notificar-ach.sh 60 confirmacion

source "$(dirname "$0")/_comun.sh"

ID_TRANSACCION=$1
TIPO=${2:-confirmacion}

if [ -z "$ID_TRANSACCION" ]; then
  echo "Uso: $0 <id_transaccion> <confirmacion|rechazo>"
  echo "Ejemplo: $0 60 confirmacion"
  exit 1
fi

if [ "$TIPO" = "confirmacion" ]; then
  echo "Enviando confirmacion ACH para transaccion $ID_TRANSACCION..."
  curl -s \
    -X POST "${TRANSFER_URL}/${ID_TRANSACCION}/confirmacion-ach" \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"referenciaConfirmacion": "ACH-CONF-OK-001"}' | jq .
elif [ "$TIPO" = "rechazo" ]; then
  echo "Enviando rechazo ACH para transaccion $ID_TRANSACCION..."
  curl -s \
    -X POST "${TRANSFER_URL}/${ID_TRANSACCION}/rechazo-ach" \
    -H "Content-Type: application/json" \
    -H "X-Gateway-Secret: $GATEWAY_SECRET" \
    -d '{"motivo": "Cuenta destino invalida en red ACH (codigo ACH_01)"}' | jq .
else
  echo "Tipo invalido: '$TIPO'. Usa 'confirmacion' o 'rechazo'."
  exit 1
fi

echo ""
