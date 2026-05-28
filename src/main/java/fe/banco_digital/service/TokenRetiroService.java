package fe.banco_digital.service;

import fe.banco_digital.dto.GenerarTokenRetiroSolicitudDTO;
import fe.banco_digital.dto.TokenRetiroEstadoDTO;
import fe.banco_digital.dto.TokenRetiroRespuestaDTO;

import java.math.BigDecimal;

public interface TokenRetiroService {

    TokenRetiroRespuestaDTO generarToken(GenerarTokenRetiroSolicitudDTO solicitud, String username);

    void usarToken(String codigo, BigDecimal monto);

    void expirarTokens();

    TokenRetiroEstadoDTO consultarEstado(String codigo);
}
