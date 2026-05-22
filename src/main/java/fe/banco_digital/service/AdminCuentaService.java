package fe.banco_digital.service;

import fe.banco_digital.dto.DecisionAperturaRespuestaDTO;
import fe.banco_digital.dto.SolicitudPendienteDTO;

import java.util.List;

public interface AdminCuentaService {

    List<SolicitudPendienteDTO> listarPendientes();

    DecisionAperturaRespuestaDTO aprobarApertura(Long idCuenta, String adminUsername);

    DecisionAperturaRespuestaDTO rechazarApertura(Long idCuenta, String adminUsername);
}
