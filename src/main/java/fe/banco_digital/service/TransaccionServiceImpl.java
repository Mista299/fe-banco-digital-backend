package fe.banco_digital.service;

import fe.banco_digital.dto.DepositoSolicitudDTO;
import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.dto.RetiroSolicitudDTO;
import fe.banco_digital.dto.TransaccionRespuestaDTO;
import fe.banco_digital.dto.TransferenciaSolicitudDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.EstadoMovimiento;
import fe.banco_digital.entity.EstadoTransferencia;
import fe.banco_digital.entity.Movimiento;
import fe.banco_digital.entity.TipoMovimiento;
import fe.banco_digital.entity.Transferencia;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.event.AuditoriaEvent;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.CuentaBloqueadaException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.exception.CuentaYaCerradaException;
import fe.banco_digital.exception.SaldoInsuficienteException;
import fe.banco_digital.mapper.TransaccionMapper;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.MovimientoRepository;
import fe.banco_digital.repository.TransferenciaExternaRepository;
import fe.banco_digital.repository.TransferenciaInternacionalRepository;
import fe.banco_digital.repository.TransferenciaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransaccionServiceImpl implements TransaccionService {

    private final MovimientoRepository movimientoRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final TransferenciaExternaRepository transferenciaExternaRepository;
    private final TransferenciaInternacionalRepository transferenciaInternacionalRepository;
    private final TransaccionMapper transaccionMapper;
    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RegistroFalloService registroFalloService;

    public TransaccionServiceImpl(MovimientoRepository movimientoRepository,
                                   TransferenciaRepository transferenciaRepository,
                                   TransferenciaExternaRepository transferenciaExternaRepository,
                                   TransferenciaInternacionalRepository transferenciaInternacionalRepository,
                                   TransaccionMapper transaccionMapper,
                                   UsuarioRepository usuarioRepository,
                                   CuentaRepository cuentaRepository,
                                   ApplicationEventPublisher eventPublisher,
                                   RegistroFalloService registroFalloService) {
        this.movimientoRepository = movimientoRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.transferenciaExternaRepository = transferenciaExternaRepository;
        this.transferenciaInternacionalRepository = transferenciaInternacionalRepository;
        this.transaccionMapper = transaccionMapper;
        this.usuarioRepository = usuarioRepository;
        this.cuentaRepository = cuentaRepository;
        this.eventPublisher = eventPublisher;
        this.registroFalloService = registroFalloService;
    }

    @Override
    @Transactional
    public TransaccionRespuestaDTO depositar(DepositoSolicitudDTO solicitud, String username) {
        Usuario usuario = resolverUsuario(username);
        Cuenta cuenta = resolverCuentaConLock(solicitud.getIdCuenta(),
                usuario.getCliente().getIdCliente());
        validarCuentaOperativa(cuenta);

        cuenta.setSaldo(cuenta.getSaldo().add(solicitud.getMonto()));
        cuentaRepository.save(cuenta);

        Movimiento m = new Movimiento();
        m.setCuenta(cuenta);
        m.setTipo(TipoMovimiento.DEPOSITO);
        m.setMonto(solicitud.getMonto());
        m.setEstado(EstadoMovimiento.EXITOSO);
        m.setFecha(LocalDateTime.now());
        movimientoRepository.save(m);

        eventPublisher.publishEvent(new AuditoriaEvent(this, "DEPOSITO",
                usuario.getIdUsuario(),
                "Depósito de " + solicitud.getMonto().toPlainString()
                        + " en cuenta " + cuenta.getNumeroCuenta()));

        return new TransaccionRespuestaDTO(m.getIdMovimiento(), TipoMovimiento.DEPOSITO.name(),
                m.getMonto(), cuenta.getSaldo(), EstadoMovimiento.EXITOSO.name(), m.getFecha(),
                "Depósito realizado exitosamente.");
    }

    @Override
    @Transactional
    public TransaccionRespuestaDTO retirar(RetiroSolicitudDTO solicitud, String username) {
        Usuario usuario = resolverUsuario(username);
        Cuenta cuenta = resolverCuentaConLock(solicitud.getIdCuenta(),
                usuario.getCliente().getIdCliente());
        validarCuentaOperativa(cuenta);

        if (cuenta.getSaldo().compareTo(solicitud.getMonto()) < 0) {
            registroFalloService.registrarFalloMovimiento(cuenta, TipoMovimiento.RETIRO, solicitud.getMonto());
            throw new SaldoInsuficienteException();
        }

        cuenta.setSaldo(cuenta.getSaldo().subtract(solicitud.getMonto()));
        cuentaRepository.save(cuenta);

        Movimiento m = new Movimiento();
        m.setCuenta(cuenta);
        m.setTipo(TipoMovimiento.RETIRO);
        m.setMonto(solicitud.getMonto());
        m.setEstado(EstadoMovimiento.EXITOSO);
        m.setFecha(LocalDateTime.now());
        movimientoRepository.save(m);

        eventPublisher.publishEvent(new AuditoriaEvent(this, "RETIRO",
                usuario.getIdUsuario(),
                "Retiro de " + solicitud.getMonto().toPlainString()
                        + " de cuenta " + cuenta.getNumeroCuenta()));

        return new TransaccionRespuestaDTO(m.getIdMovimiento(), TipoMovimiento.RETIRO.name(),
                m.getMonto(), cuenta.getSaldo(), EstadoMovimiento.EXITOSO.name(), m.getFecha(),
                "Retiro realizado exitosamente.");
    }

    // Locks siempre en orden ascendente de idCuenta para prevenir deadlocks
    @Override
    @Transactional
    public TransaccionRespuestaDTO transferir(TransferenciaSolicitudDTO solicitud, String username) {
        Usuario usuario = resolverUsuario(username);

        Cuenta destinoRef = cuentaRepository
                .findByNumeroCuenta(solicitud.getNumeroCuentaDestino())
                .orElseThrow(() -> new CuentaNoEncontradaException(-1L));

        Long idOrigen = solicitud.getIdCuentaOrigen();
        Long idDestino = destinoRef.getIdCuenta();

        cuentaRepository.findByIdCuentaAndCliente_IdCliente(idOrigen,
                usuario.getCliente().getIdCliente()).orElseThrow(AccesoNoAutorizadoException::new);

        Cuenta primerLock = cuentaRepository
                .findByIdCuentaConLock(Math.min(idOrigen, idDestino))
                .orElseThrow(() -> new CuentaNoEncontradaException(Math.min(idOrigen, idDestino)));
        Cuenta segundoLock = cuentaRepository
                .findByIdCuentaConLock(Math.max(idOrigen, idDestino))
                .orElseThrow(() -> new CuentaNoEncontradaException(Math.max(idOrigen, idDestino)));

        Cuenta origen  = primerLock.getIdCuenta().equals(idOrigen) ? primerLock : segundoLock;
        Cuenta destino = primerLock.getIdCuenta().equals(idDestino) ? primerLock : segundoLock;

        validarCuentaOperativa(origen);
        if (destino.getEstado() != EstadoCuenta.ACTIVA) {
            registroFalloService.registrarFalloTransferencia(origen, destino, solicitud.getMonto());
            throw new CuentaBloqueadaException(destino.getNumeroCuenta());
        }
        if (origen.getSaldo().compareTo(solicitud.getMonto()) < 0) {
            registroFalloService.registrarFalloTransferencia(origen, destino, solicitud.getMonto());
            throw new SaldoInsuficienteException();
        }

        origen.setSaldo(origen.getSaldo().subtract(solicitud.getMonto()));
        destino.setSaldo(destino.getSaldo().add(solicitud.getMonto()));
        cuentaRepository.save(origen);
        cuentaRepository.save(destino);

        Transferencia t = new Transferencia();
        t.setCuentaOrigen(origen);
        t.setCuentaDestino(destino);
        t.setMonto(solicitud.getMonto());
        t.setEstado(EstadoTransferencia.EXITOSA);
        t.setFecha(LocalDateTime.now());
        transferenciaRepository.save(t);

        eventPublisher.publishEvent(new AuditoriaEvent(this, "TRANSFERENCIA",
                usuario.getIdUsuario(),
                "Transferencia de " + solicitud.getMonto().toPlainString()
                        + " desde " + origen.getNumeroCuenta()
                        + " hacia " + destino.getNumeroCuenta()));

        return new TransaccionRespuestaDTO(t.getIdTransferencia(), "TRANSFERENCIA",
                t.getMonto(), origen.getSaldo(), EstadoTransferencia.EXITOSA.name(), t.getFecha(),
                "Transferencia realizada exitosamente.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDTO> obtenerMovimientos(Long idCuenta, String username) {
        verificarPropietario(idCuenta, username);
        return transaccionMapper.aListaDTOUnificada(
                movimientoRepository.findByCuenta_IdCuentaOrderByFechaDesc(idCuenta),
                transferenciaRepository.findByCuentaIdOrderByFechaDesc(idCuenta),
                idCuenta,
                transferenciaExternaRepository.findByCuentaOrigen_IdCuentaOrderByFechaDesc(idCuenta),
                transferenciaInternacionalRepository.findByCuentaOrigen_IdCuentaOrderByFechaDesc(idCuenta));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDTO> obtenerMovimientosPorFecha(Long idCuenta,
            LocalDateTime fechaInicio, LocalDateTime fechaFin, String username) {
        verificarPropietario(idCuenta, username);
        return transaccionMapper.aListaDTOUnificada(
                movimientoRepository.findByCuenta_IdCuentaAndFechaBetweenOrderByFechaDesc(idCuenta, fechaInicio, fechaFin),
                transferenciaRepository.findByCuentaIdAndFechaBetweenOrderByFechaDesc(idCuenta, fechaInicio, fechaFin),
                idCuenta,
                transferenciaExternaRepository.findByCuentaOrigen_IdCuentaAndFechaBetweenOrderByFechaDesc(idCuenta, fechaInicio, fechaFin),
                transferenciaInternacionalRepository.findByCuentaOrigen_IdCuentaAndFechaBetweenOrderByFechaDesc(idCuenta, fechaInicio, fechaFin));
    }

    private Usuario resolverUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);
    }

    private Cuenta resolverCuentaConLock(Long idCuenta, Long idCliente) {
        cuentaRepository.findByIdCuentaAndCliente_IdCliente(idCuenta, idCliente)
                .orElseThrow(AccesoNoAutorizadoException::new);
        return cuentaRepository.findByIdCuentaConLock(idCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(idCuenta));
    }

    private void validarCuentaOperativa(Cuenta cuenta) {
        if (cuenta.getEstado() == EstadoCuenta.BLOQUEADA)
            throw new CuentaBloqueadaException(cuenta.getNumeroCuenta());
        if (cuenta.getEstado() == EstadoCuenta.INACTIVA)
            throw new CuentaYaCerradaException(cuenta.getNumeroCuenta());
    }

    private void verificarPropietario(Long idCuenta, String username) {
        Usuario usuario = resolverUsuario(username);
        cuentaRepository.findByIdCuentaAndCliente_IdCliente(idCuenta,
                usuario.getCliente().getIdCliente()).orElseThrow(AccesoNoAutorizadoException::new);
    }
}
