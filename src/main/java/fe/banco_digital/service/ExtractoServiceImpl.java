package fe.banco_digital.service;

import fe.banco_digital.dto.ExtractoDatosDTO;
import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.PeriodoInvalidoException;
import fe.banco_digital.exception.PeriodoNoDisponibleException;
import fe.banco_digital.mapper.TransaccionMapper;
import fe.banco_digital.pdf.ExtractoPdfGenerador;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.MovimientoRepository;
import fe.banco_digital.repository.TransferenciaExternaRepository;
import fe.banco_digital.repository.TransferenciaInternacionalRepository;
import fe.banco_digital.repository.TransferenciaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class ExtractoServiceImpl implements ExtractoService {

    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final TransferenciaExternaRepository transferenciaExternaRepository;
    private final TransferenciaInternacionalRepository transferenciaInternacionalRepository;
    private final TransaccionMapper transaccionMapper;
    private final ExtractoPdfGenerador pdfGenerador;

    public ExtractoServiceImpl(UsuarioRepository usuarioRepository,
                                CuentaRepository cuentaRepository,
                                MovimientoRepository movimientoRepository,
                                TransferenciaRepository transferenciaRepository,
                                TransferenciaExternaRepository transferenciaExternaRepository,
                                TransferenciaInternacionalRepository transferenciaInternacionalRepository,
                                TransaccionMapper transaccionMapper,
                                ExtractoPdfGenerador pdfGenerador) {
        this.usuarioRepository = usuarioRepository;
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.transferenciaExternaRepository = transferenciaExternaRepository;
        this.transferenciaInternacionalRepository = transferenciaInternacionalRepository;
        this.transaccionMapper = transaccionMapper;
        this.pdfGenerador = pdfGenerador;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarExtracto(Long idCuenta, int anio, int mes, String username) {
        validarParametros(anio, mes);

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        Cuenta cuenta = cuentaRepository
                .findByIdCuentaAndCliente_IdCliente(idCuenta, usuario.getCliente().getIdCliente())
                .orElseThrow(AccesoNoAutorizadoException::new);

        LocalDateTime inicio = LocalDateTime.of(anio, mes, 1, 0, 0, 0);
        LocalDateTime fin = inicio.with(TemporalAdjusters.lastDayOfMonth())
                .withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
        LocalDateTime ahora = LocalDateTime.now();

        List<MovimientoDTO> movsPeriodo    = obtenerMovimientos(idCuenta, inicio, fin);
        List<MovimientoDTO> movsPosteriores = obtenerMovimientos(idCuenta, fin.plusNanos(1), ahora);

        BigDecimal sumaPosteriores = sumar(movsPosteriores);
        BigDecimal saldoFinal      = cuenta.getSaldo().subtract(sumaPosteriores);
        BigDecimal sumaPeriodo     = sumar(movsPeriodo);
        BigDecimal saldoInicial    = saldoFinal.subtract(sumaPeriodo);

        BigDecimal totalCreditos = movsPeriodo.stream()
                .map(MovimientoDTO::getMonto)
                .filter(m -> m.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitos = movsPeriodo.stream()
                .map(MovimientoDTO::getMonto)
                .filter(m -> m.compareTo(BigDecimal.ZERO) < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Cliente cliente = cuenta.getCliente();

        ExtractoDatosDTO datos = new ExtractoDatosDTO();
        datos.setNumeroCuentaEnmascarado(enmascararCuenta(cuenta.getNumeroCuenta()));
        datos.setTipoCuenta(cuenta.getTipo().name());
        datos.setNombreTitular(cliente.getNombre());
        datos.setDocumento(cliente.getDocumento());
        datos.setAnio(anio);
        datos.setMes(mes);
        datos.setSaldoInicial(saldoInicial);
        datos.setSaldoFinal(saldoFinal);
        datos.setTotalCreditos(totalCreditos);
        datos.setTotalDebitos(totalDebitos);
        datos.setMovimientos(movsPeriodo);

        return pdfGenerador.generar(datos);
    }

    private static final int ANIO_MIN = 2020;

    private void validarParametros(int anio, int mes) {
        if (mes < 1 || mes > 12) {
            throw new PeriodoInvalidoException("El mes debe estar entre 1 y 12");
        }
        if (anio < ANIO_MIN) {
            throw new PeriodoInvalidoException("El año no puede ser anterior a " + ANIO_MIN);
        }
        LocalDate hoy = LocalDate.now();
        if (anio > hoy.getYear()
                || (anio == hoy.getYear() && mes > hoy.getMonthValue())) {
            throw new PeriodoInvalidoException("No se pueden generar extractos de fechas futuras");
        }
        if (anio == hoy.getYear() && mes == hoy.getMonthValue()) {
            throw new PeriodoNoDisponibleException();
        }
    }

    private List<MovimientoDTO> obtenerMovimientos(Long idCuenta,
                                                    LocalDateTime desde, LocalDateTime hasta) {
        return transaccionMapper.aListaDTOUnificada(
                movimientoRepository
                        .findByCuenta_IdCuentaAndFechaBetweenOrderByFechaDesc(idCuenta, desde, hasta),
                transferenciaRepository
                        .findByCuentaIdAndFechaBetweenOrderByFechaDesc(idCuenta, desde, hasta),
                idCuenta,
                transferenciaExternaRepository
                        .findByCuentaOrigen_IdCuentaAndFechaBetweenOrderByFechaDesc(idCuenta, desde, hasta),
                transferenciaInternacionalRepository
                        .findByCuentaOrigen_IdCuentaAndFechaBetweenOrderByFechaDesc(idCuenta, desde, hasta));
    }

    private BigDecimal sumar(List<MovimientoDTO> movimientos) {
        return movimientos.stream()
                .map(MovimientoDTO::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String enmascararCuenta(String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.length() < 4) return numeroCuenta;
        return "****" + numeroCuenta.substring(numeroCuenta.length() - 4);
    }
}
