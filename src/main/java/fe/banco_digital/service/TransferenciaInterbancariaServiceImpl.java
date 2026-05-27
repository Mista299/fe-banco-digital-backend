package fe.banco_digital.service;

import fe.banco_digital.dto.ConfirmacionAchSolicitudDTO;
import fe.banco_digital.dto.RechazoAchSolicitudDTO;
import fe.banco_digital.dto.TransferenciaInterbancariaResponseDTO;
import fe.banco_digital.dto.TransferenciaInterbancariaSolicitudDTO;
import fe.banco_digital.dto.ValidacionTransaccionResponseDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoMovimiento;
import fe.banco_digital.entity.EstadoTransferenciaExterna;
import fe.banco_digital.entity.Movimiento;
import fe.banco_digital.entity.TipoMovimiento;
import fe.banco_digital.entity.TransferenciaExterna;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.event.AuditoriaEvent;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.exception.OperacionNoPermitidaException;
import fe.banco_digital.exception.SaldoInsuficienteException;
import fe.banco_digital.exception.TransaccionNoEncontradaException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.MovimientoRepository;
import fe.banco_digital.repository.TransferenciaExternaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferenciaInterbancariaServiceImpl implements TransferenciaInterbancariaService {

    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final TransferenciaExternaRepository transferenciaExternaRepository;
    private final MovimientoRepository movimientoRepository;
    private final MotorValidacionService motorValidacionService;
    private final RegistroFalloService registroFalloService;
    private final ApplicationEventPublisher eventPublisher;

    public TransferenciaInterbancariaServiceImpl(UsuarioRepository usuarioRepository,
                                                 CuentaRepository cuentaRepository,
                                                 TransferenciaExternaRepository transferenciaExternaRepository,
                                                 MovimientoRepository movimientoRepository,
                                                 MotorValidacionService motorValidacionService,
                                                 RegistroFalloService registroFalloService,
                                                 ApplicationEventPublisher eventPublisher) {
        this.usuarioRepository = usuarioRepository;
        this.cuentaRepository = cuentaRepository;
        this.transferenciaExternaRepository = transferenciaExternaRepository;
        this.movimientoRepository = movimientoRepository;
        this.motorValidacionService = motorValidacionService;
        this.registroFalloService = registroFalloService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public TransferenciaInterbancariaResponseDTO iniciarTransferencia(
            TransferenciaInterbancariaSolicitudDTO solicitud, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        cuentaRepository.findByIdCuentaAndCliente_IdCliente(solicitud.getIdCuentaOrigen(),
                        usuario.getCliente().getIdCliente())
                .orElseThrow(AccesoNoAutorizadoException::new);

        Cuenta origen = cuentaRepository.findByIdCuentaConLock(solicitud.getIdCuentaOrigen())
                .orElseThrow(() -> new CuentaNoEncontradaException(solicitud.getIdCuentaOrigen()));

        ValidacionTransaccionResponseDTO validacion =
                motorValidacionService.validarCuentaParaDebito(origen, solicitud.getMonto());

        if (!validacion.isAutorizada()) {
            registroFalloService.registrarFalloMovimiento(origen, TipoMovimiento.RETIRO, solicitud.getMonto());
            if ("SALDO_INSUFICIENTE".equals(validacion.getCodigo())) {
                throw new SaldoInsuficienteException();
            }
            throw new OperacionNoPermitidaException(validacion.getMensaje());
        }

        origen.setSaldo(origen.getSaldo().subtract(solicitud.getMonto()));
        cuentaRepository.save(origen);

        TransferenciaExterna te = new TransferenciaExterna();
        te.setCuentaOrigen(origen);
        te.setBancoDestino(solicitud.getBancoDestino());
        te.setTipoCuentaDestino(solicitud.getTipoCuentaDestino());
        te.setNumeroCuentaDestino(solicitud.getNumeroCuentaDestino());
        te.setTipoDocumentoReceptor(solicitud.getTipoDocumentoReceptor());
        te.setNumeroDocumentoReceptor(solicitud.getNumeroDocumentoReceptor());
        te.setNombreReceptor(solicitud.getNombreReceptor());
        te.setMonto(solicitud.getMonto());
        te.setEstado(EstadoTransferenciaExterna.PENDIENTE_PROCESAMIENTO);
        te.setFecha(LocalDateTime.now());
        te.setReferenciaExterna("ACH-" + UUID.randomUUID());
        transferenciaExternaRepository.save(te);

        eventPublisher.publishEvent(new AuditoriaEvent(this, "TRANSFERENCIA_INTERBANCARIA_ACH",
                usuario.getIdUsuario(),
                "Orden ACH enviada a " + solicitud.getBancoDestino()
                        + " desde cuenta " + origen.getNumeroCuenta()));

        return construirRespuesta(te, origen.getSaldo(),
                "Transferencia enviada a la red ACH. Estado: Pendiente de Procesamiento.");
    }

    @Override
    @Transactional
    public TransferenciaInterbancariaResponseDTO registrarRechazoAch(Long idTransaccion,
                                                                     RechazoAchSolicitudDTO solicitud) {
        TransferenciaExterna te = transferenciaExternaRepository.findById(idTransaccion)
                .orElseThrow(() -> new TransaccionNoEncontradaException(idTransaccion));

        if (te.getEstado() != EstadoTransferenciaExterna.PENDIENTE_PROCESAMIENTO) {
            throw new OperacionNoPermitidaException("Solo se pueden reversar transacciones pendientes de procesamiento ACH.");
        }

        Cuenta origen = cuentaRepository.findByIdCuentaConLock(te.getCuentaOrigen().getIdCuenta())
                .orElseThrow(() -> new CuentaNoEncontradaException(te.getCuentaOrigen().getIdCuenta()));

        origen.setSaldo(origen.getSaldo().add(te.getMonto()));
        cuentaRepository.save(origen);

        te.setEstado(EstadoTransferenciaExterna.REVERSADA);
        te.setMotivoRechazo(solicitud.getMotivo());
        transferenciaExternaRepository.save(te);

        Movimiento devolucion = new Movimiento();
        devolucion.setCuenta(origen);
        devolucion.setTipo(TipoMovimiento.DEPOSITO);
        devolucion.setMonto(te.getMonto());
        devolucion.setEstado(EstadoMovimiento.EXITOSO);
        devolucion.setFecha(LocalDateTime.now());
        devolucion.setDescripcion("Reversión ACH: " + te.getReferenciaExterna());
        movimientoRepository.save(devolucion);

        Long idUsuarioAuditoria = usuarioRepository.findByCliente_IdCliente(origen.getCliente().getIdCliente())
                .map(Usuario::getIdUsuario)
                .orElse(null);
        if (idUsuarioAuditoria != null) {
            eventPublisher.publishEvent(new AuditoriaEvent(this, "REVERSO_ACH", idUsuarioAuditoria,
                    "Reversión ACH de transacción " + idTransaccion + ". Motivo: " + solicitud.getMotivo()));
        }

        return construirRespuesta(te, origen.getSaldo(),
                "La red ACH rechazó la operación. Se reversó automáticamente el valor al saldo del emisor.");
    }

    @Override
    @Transactional
    public TransferenciaInterbancariaResponseDTO registrarConfirmacionAch(Long idTransaccion,
                                                                          ConfirmacionAchSolicitudDTO solicitud) {
        TransferenciaExterna te = transferenciaExternaRepository.findById(idTransaccion)
                .orElseThrow(() -> new TransaccionNoEncontradaException(idTransaccion));

        if (te.getEstado() != EstadoTransferenciaExterna.PENDIENTE_PROCESAMIENTO) {
            throw new OperacionNoPermitidaException("Solo se pueden confirmar transacciones pendientes de procesamiento ACH.");
        }

        te.setEstado(EstadoTransferenciaExterna.EXITOSA);
        if (solicitud.getReferenciaConfirmacion() != null) {
            te.setReferenciaExterna(solicitud.getReferenciaConfirmacion());
        }
        transferenciaExternaRepository.save(te);

        Long idUsuarioAuditoria = usuarioRepository.findByCliente_IdCliente(te.getCuentaOrigen().getCliente().getIdCliente())
                .map(Usuario::getIdUsuario)
                .orElse(null);
        if (idUsuarioAuditoria != null) {
            eventPublisher.publishEvent(new AuditoriaEvent(this, "CONFIRMACION_ACH", idUsuarioAuditoria,
                    "Confirmación ACH de transacción " + idTransaccion
                            + ". Referencia: " + te.getReferenciaExterna()));
        }

        Cuenta origen = cuentaRepository.findById(te.getCuentaOrigen().getIdCuenta())
                .orElseThrow(() -> new CuentaNoEncontradaException(te.getCuentaOrigen().getIdCuenta()));

        return construirRespuesta(te, origen.getSaldo(),
                "La red ACH confirmó la transferencia exitosamente.");
    }

    @Override
    @Transactional(readOnly = true)
    public TransferenciaInterbancariaResponseDTO consultarTransferencia(Long idTransaccion, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        TransferenciaExterna te = transferenciaExternaRepository.findById(idTransaccion)
                .orElseThrow(() -> new TransaccionNoEncontradaException(idTransaccion));

        if (!te.getCuentaOrigen().getCliente().getIdCliente()
                .equals(usuario.getCliente().getIdCliente())) {
            throw new AccesoNoAutorizadoException();
        }

        java.math.BigDecimal saldo = cuentaRepository
                .findById(te.getCuentaOrigen().getIdCuenta())
                .map(Cuenta::getSaldo)
                .orElse(java.math.BigDecimal.ZERO);

        return construirRespuesta(te, saldo, "Estado ACH.");
    }

    private TransferenciaInterbancariaResponseDTO construirRespuesta(TransferenciaExterna te,
                                                                     java.math.BigDecimal saldoResultante,
                                                                     String mensaje) {
        return new TransferenciaInterbancariaResponseDTO(te.getIdTransfExt(),
                te.getReferenciaExterna(), te.getMonto(), saldoResultante,
                te.getEstado().name(), te.getFecha(), mensaje);
    }
}
