package fe.banco_digital.service;

import fe.banco_digital.dto.ReporteConsolidadoDTO;
import fe.banco_digital.dto.ReporteEstadoCuentaDTO;
import fe.banco_digital.dto.SaldoTiempoRealDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.TipoCuenta;
import fe.banco_digital.repository.CuentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ReporteSaldoServiceImpl implements ReporteSaldoService {

    private final CuentaRepository cuentaRepository;

    public ReporteSaldoServiceImpl(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteConsolidadoDTO obtenerConsolidadoGlobal() {

        BigDecimal totalSistema =
                cuentaRepository.obtenerSaldoTotalSistema();

        BigDecimal totalAhorros =
                cuentaRepository.obtenerSaldoPorTipo(TipoCuenta.AHORROS);

        BigDecimal totalCorriente =
                cuentaRepository.obtenerSaldoPorTipo(TipoCuenta.CORRIENTE);

        return new ReporteConsolidadoDTO(
                totalSistema,
                totalAhorros,
                totalCorriente
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteEstadoCuentaDTO> filtrarPorEstado(EstadoCuenta estado) {

        List<Cuenta> cuentas =
                cuentaRepository.findByEstadoConCliente(estado);

        return cuentas.stream()
                .map(c -> new ReporteEstadoCuentaDTO(
                        c.getCliente().getNombre(),
                        c.getNumeroCuenta(),
                        c.getSaldo(),
                        c.getEstado().name()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaldoTiempoRealDTO> obtenerSaldosTiempoReal() {

        return cuentaRepository.findAll()
                .stream()
                .map(c -> new SaldoTiempoRealDTO(
                        c.getIdCuenta(),
                        c.getSaldo(),
                        c.getSaldo(),
                        c.getEstado().name(),
                        c.getTipo().name()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaldoTiempoRealDTO> filtrarPorRango(
            BigDecimal min,
            BigDecimal max) {

        List<Cuenta> cuentas;

        if (max != null) {
            cuentas = cuentaRepository.findBySaldoBetween(min, max);
        } else {
            cuentas = cuentaRepository.findBySaldoGreaterThan(min);
        }

        return cuentas.stream()
                .map(c -> new SaldoTiempoRealDTO(
                        c.getIdCuenta(),
                        c.getSaldo(),
                        c.getSaldo(),
                        c.getEstado().name(),
                        c.getTipo().name()
                ))
                .toList();
    }
}