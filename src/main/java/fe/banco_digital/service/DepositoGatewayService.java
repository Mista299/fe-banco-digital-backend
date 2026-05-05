package fe.banco_digital.service;

import fe.banco_digital.dto.ComprobanteDepositoDTO;
import fe.banco_digital.dto.NotificacionDepositoDTO;

public interface DepositoGatewayService {

    ComprobanteDepositoDTO procesarNotificacion(NotificacionDepositoDTO notificacion);
}
