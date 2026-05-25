package fe.banco_digital.service;

import fe.banco_digital.dto.ReporteResumenDTO;

import java.time.LocalDateTime;

public interface ReporteService {

    ReporteResumenDTO generarReporte(LocalDateTime inicio, LocalDateTime fin);

    byte[] exportarCSV(LocalDateTime inicio, LocalDateTime fin);
}