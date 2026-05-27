package fe.banco_digital.service;

import fe.banco_digital.dto.DepositoPendienteRespuestaDTO;
import fe.banco_digital.dto.RegistrarDepositoPendienteDTO;

public interface DepositoPendienteService {
    DepositoPendienteRespuestaDTO registrar(RegistrarDepositoPendienteDTO solicitud, String username);
    DepositoPendienteRespuestaDTO consultar(String referenciaGateway, String username);
}
