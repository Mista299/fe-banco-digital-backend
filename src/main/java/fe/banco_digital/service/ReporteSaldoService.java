package fe.banco_digital.service;

import fe.banco_digital.dto.ReporteConsolidadoDTO;
import fe.banco_digital.dto.ReporteEstadoCuentaDTO;
import fe.banco_digital.dto.SaldoTiempoRealDTO;
import fe.banco_digital.entity.EstadoCuenta;

import java.math.BigDecimal;
import java.util.List;

public interface ReporteSaldoService {

    ReporteConsolidadoDTO obtenerConsolidadoGlobal();

    List<ReporteEstadoCuentaDTO> filtrarPorEstado(EstadoCuenta estado);

    List<SaldoTiempoRealDTO> obtenerSaldosTiempoReal();

    List<SaldoTiempoRealDTO> filtrarPorRango(
            BigDecimal min,
            BigDecimal max);
}