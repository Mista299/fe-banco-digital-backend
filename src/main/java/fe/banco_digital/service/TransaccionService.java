package fe.banco_digital.service;

import fe.banco_digital.dto.MovimientoDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface TransaccionService {

    List<MovimientoDTO> obtenerMovimientos(Long idCuenta, String username);

    List<MovimientoDTO> obtenerMovimientosPorFecha(
            Long idCuenta,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            String username);
}
