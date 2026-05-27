package fe.banco_digital.service;

import fe.banco_digital.dto.ConfirmacionSwiftSolicitudDTO;
import fe.banco_digital.dto.RechazoSwiftSolicitudDTO;
import fe.banco_digital.dto.TransferenciaInternacionalResponseDTO;
import fe.banco_digital.dto.TransferenciaInternacionalSolicitudDTO;

public interface TransferenciaInternacionalService {

    TransferenciaInternacionalResponseDTO iniciarTransferencia(
            TransferenciaInternacionalSolicitudDTO solicitud, String username);

    TransferenciaInternacionalResponseDTO registrarConfirmacionSwift(
            Long idTransfInt, ConfirmacionSwiftSolicitudDTO solicitud);

    TransferenciaInternacionalResponseDTO registrarRechazoSwift(
            Long idTransfInt, RechazoSwiftSolicitudDTO solicitud);

    TransferenciaInternacionalResponseDTO consultarTransferencia(
            Long idTransfInt, String username);
}
