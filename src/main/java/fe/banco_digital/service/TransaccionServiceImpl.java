package fe.banco_digital.service;

import fe.banco_digital.dto.DepositoSolicitudDTO;
import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.dto.RetiroSolicitudDTO;
import fe.banco_digital.dto.TransaccionRespuestaDTO;
import fe.banco_digital.dto.TransferenciaSolicitudDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.EstadoTransaccion;
import fe.banco_digital.entity.TipoTransaccion;
import fe.banco_digital.entity.Transaccion;
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
import fe.banco_digital.repository.TransaccionRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransaccionServiceImpl implements TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final TransaccionMapper transaccionMapper;
    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RegistroFalloService registroFalloService;

    public TransaccionServiceImpl(TransaccionRepository transaccionRepository,
                                   TransaccionMapper transaccionMapper,
                                   UsuarioRepository usuarioRepository,
                                   CuentaRepository cuentaRepository,
                                   ApplicationEventPublisher eventPublisher,
                                   RegistroFalloService registroFalloService) {
        this.transaccionRepository = transaccionRepository;
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

        Transaccion t = registrarTransaccion(null, cuenta,
                TipoTransaccion.DEPOSITO, solicitud.getMonto(), EstadoTransaccion.EXITOSA);

        String montoDeposito = solicitud.getMonto().toPlainString();
        eventPublisher.publishEvent(new AuditoriaEvent(this, "DEPOSITO",
                usuario.getIdUsuario(),
                "Depósito de " + montoDeposito + " en cuenta " + cuenta.getNumeroCuenta()));

        return construirRespuesta(t, cuenta.getSaldo(), "Depósito realizado exitosamente.");
    }

    @Override
    @Transactional
    public TransaccionRespuestaDTO retirar(RetiroSolicitudDTO solicitud, String username) {
        Usuario usuario = resolverUsuario(username);
        Cuenta cuenta = resolverCuentaConLock(solicitud.getIdCuenta(),
                usuario.getCliente().getIdCliente());
        validarCuentaOperativa(cuenta);

        if (cuenta.getSaldo().compareTo(solicitud.getMonto()) < 0) {
            registroFalloService.registrarFallo(cuenta, null,
                    TipoTransaccion.RETIRO, solicitud.getMonto());
            throw new SaldoInsuficienteException();
        }

        cuenta.setSaldo(cuenta.getSaldo().subtract(solicitud.getMonto()));
        cuentaRepository.save(cuenta);

        Transaccion t = registrarTransaccion(cuenta, null,
                TipoTransaccion.RETIRO, solicitud.getMonto(), EstadoTransaccion.EXITOSA);

        String montoRetiro = solicitud.getMonto().toPlainString();
        eventPublisher.publishEvent(new AuditoriaEvent(this, "RETIRO",
                usuario.getIdUsuario(),
                "Retiro de " + montoRetiro + " de cuenta " + cuenta.getNumeroCuenta()));

        return construirRespuesta(t, cuenta.getSaldo(), "Retiro realizado exitosamente.");
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
            registroFalloService.registrarFallo(origen, destino,
                    TipoTransaccion.TRANSFERENCIA, solicitud.getMonto());
            throw new CuentaBloqueadaException(destino.getNumeroCuenta());
        }
        if (origen.getSaldo().compareTo(solicitud.getMonto()) < 0) {
            registroFalloService.registrarFallo(origen, destino,
                    TipoTransaccion.TRANSFERENCIA, solicitud.getMonto());
            throw new SaldoInsuficienteException();
        }

        origen.setSaldo(origen.getSaldo().subtract(solicitud.getMonto()));
        destino.setSaldo(destino.getSaldo().add(solicitud.getMonto()));
        cuentaRepository.save(origen);
        cuentaRepository.save(destino);

        Transaccion t = registrarTransaccion(origen, destino,
                TipoTransaccion.TRANSFERENCIA, solicitud.getMonto(), EstadoTransaccion.EXITOSA);

        String montoTransferencia = solicitud.getMonto().toPlainString();
        eventPublisher.publishEvent(new AuditoriaEvent(this, "TRANSFERENCIA",
                usuario.getIdUsuario(),
                "Transferencia de " + montoTransferencia
                        + " desde " + origen.getNumeroCuenta()
                        + " hacia " + destino.getNumeroCuenta()));

        return construirRespuesta(t, origen.getSaldo(), "Transferencia realizada exitosamente.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDTO> obtenerMovimientos(Long idCuenta, String username) {
        verificarPropietario(idCuenta, username);
        return transaccionMapper.aListaDTO(
                transaccionRepository.findByCuentaIdOrderByFechaDesc(idCuenta), idCuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDTO> obtenerMovimientosPorFecha(Long idCuenta,
            LocalDateTime fechaInicio, LocalDateTime fechaFin, String username) {
        verificarPropietario(idCuenta, username);
        return transaccionMapper.aListaDTO(
                transaccionRepository.findByCuentaIdAndFechaBetweenOrderByFechaDesc(
                        idCuenta, fechaInicio, fechaFin), idCuenta);
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

    private Transaccion registrarTransaccion(Cuenta origen, Cuenta destino,
            TipoTransaccion tipo, BigDecimal monto, EstadoTransaccion estado) {
        Transaccion t = new Transaccion();
        t.setCuentaOrigen(origen);
        t.setCuentaDestino(destino);
        t.setTipo(tipo);
        t.setMonto(monto);
        t.setEstado(estado);
        t.setFecha(LocalDateTime.now());
        return transaccionRepository.save(t);
    }

    private TransaccionRespuestaDTO construirRespuesta(Transaccion t,
            BigDecimal saldoResultante, String mensaje) {
        return new TransaccionRespuestaDTO(t.getIdTransaccion(), t.getTipo().name(),
                t.getMonto(), saldoResultante, t.getEstado().name(), t.getFecha(), mensaje);
    }

    private void verificarPropietario(Long idCuenta, String username) {
        Usuario usuario = resolverUsuario(username);
        cuentaRepository.findByIdCuentaAndCliente_IdCliente(idCuenta,
                usuario.getCliente().getIdCliente()).orElseThrow(AccesoNoAutorizadoException::new);
    }
}
