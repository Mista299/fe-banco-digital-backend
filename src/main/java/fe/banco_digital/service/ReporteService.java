package fe.banco_digital.service;

import fe.banco_digital.dto.ReporteMovimientoDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface ReporteService {

    List<ReporteMovimientoDTO> generarReporte(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    byte[] exportarCSV(
            LocalDateTime inicio,
            LocalDateTime fin
    );
}