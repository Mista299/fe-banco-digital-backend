package fe.banco_digital.service;

import fe.banco_digital.dto.ConfirmacionAchSolicitudDTO;
import fe.banco_digital.dto.RechazoAchSolicitudDTO;
import fe.banco_digital.dto.TransferenciaInterbancariaResponseDTO;
import fe.banco_digital.dto.TransferenciaInterbancariaSolicitudDTO;

public interface TransferenciaInterbancariaService {
    TransferenciaInterbancariaResponseDTO iniciarTransferencia(TransferenciaInterbancariaSolicitudDTO solicitud, String username);
    TransferenciaInterbancariaResponseDTO registrarRechazoAch(Long idTransaccion, RechazoAchSolicitudDTO solicitud);
    TransferenciaInterbancariaResponseDTO registrarConfirmacionAch(Long idTransaccion, ConfirmacionAchSolicitudDTO solicitud);
    TransferenciaInterbancariaResponseDTO consultarTransferencia(Long idTransaccion, String username);
}
