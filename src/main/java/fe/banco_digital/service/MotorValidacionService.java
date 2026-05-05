package fe.banco_digital.service;

import fe.banco_digital.dto.ValidacionTransaccionResponseDTO;
import fe.banco_digital.dto.ValidacionTransaccionSolicitudDTO;
import fe.banco_digital.entity.Cuenta;

import java.math.BigDecimal;

public interface MotorValidacionService {
    ValidacionTransaccionResponseDTO validar(ValidacionTransaccionSolicitudDTO solicitud, String username);
    ValidacionTransaccionResponseDTO validarCuentaParaDebito(Cuenta cuenta, BigDecimal monto);
}
