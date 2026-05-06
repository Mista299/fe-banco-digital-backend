package fe.banco_digital.service;

import fe.banco_digital.dto.DepositoSolicitudDTO;
import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.dto.RetiroSolicitudDTO;
import fe.banco_digital.dto.TransaccionRespuestaDTO;
import fe.banco_digital.dto.TransferenciaSolicitudDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface TransaccionService {

    TransaccionRespuestaDTO depositar(DepositoSolicitudDTO solicitud, String username);

    TransaccionRespuestaDTO retirar(RetiroSolicitudDTO solicitud, String username);

    TransaccionRespuestaDTO transferir(TransferenciaSolicitudDTO solicitud, String username);

    List<MovimientoDTO> obtenerMovimientos(Long idCuenta, String username);

    List<MovimientoDTO> obtenerMovimientosPorFecha(
            Long idCuenta,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            String username);
}
