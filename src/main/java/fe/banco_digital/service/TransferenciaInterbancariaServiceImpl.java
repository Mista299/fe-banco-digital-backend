package fe.banco_digital.service;

import fe.banco_digital.dto.ConfirmacionAchSolicitudDTO;
import fe.banco_digital.dto.RechazoAchSolicitudDTO;
import fe.banco_digital.dto.TransferenciaInterbancariaResponseDTO;
import fe.banco_digital.dto.TransferenciaInterbancariaSolicitudDTO;
import fe.banco_digital.dto.ValidacionTransaccionResponseDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoTransaccion;
import fe.banco_digital.entity.TipoTransaccion;
import fe.banco_digital.entity.Transaccion;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.event.AuditoriaEvent;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.exception.OperacionNoPermitidaException;
import fe.banco_digital.exception.TransaccionNoEncontradaException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.TransaccionRepository;
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
    private final TransaccionRepository transaccionRepository;
    private final MotorValidacionService motorValidacionService;
    private final RegistroFalloService registroFalloService;
    private final ApplicationEventPublisher eventPublisher;

    public TransferenciaInterbancariaServiceImpl(UsuarioRepository usuarioRepository,
                                                 CuentaRepository cuentaRepository,
                                                 TransaccionRepository transaccionRepository,
                                                 MotorValidacionService motorValidacionService,
                                                 RegistroFalloService registroFalloService,
                                                 ApplicationEventPublisher eventPublisher) {
        this.usuarioRepository = usuarioRepository;
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
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
            registroFalloService.registrarFallo(origen, null,
                    TipoTransaccion.TRANSFERENCIA_INTERBANCARIA, solicitud.getMonto());
            throw new OperacionNoPermitidaException(validacion.getMensaje());
        }

        origen.setSaldo(origen.getSaldo().subtract(solicitud.getMonto()));
        cuentaRepository.save(origen);

        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaOrigen(origen);
        transaccion.setCuentaDestino(null);
        transaccion.setTipo(TipoTransaccion.TRANSFERENCIA_INTERBANCARIA);
        transaccion.setMonto(solicitud.getMonto());
        transaccion.setEstado(EstadoTransaccion.PENDIENTE_PROCESAMIENTO);
        transaccion.setFecha(LocalDateTime.now());
        transaccion.setBancoDestino(solicitud.getBancoDestino());
        transaccion.setTipoCuentaDestinoExterna(solicitud.getTipoCuentaDestino());
        transaccion.setNumeroCuentaDestinoExterna(solicitud.getNumeroCuentaDestino());
        transaccion.setTipoDocumentoReceptor(solicitud.getTipoDocumentoReceptor());
        transaccion.setNumeroDocumentoReceptor(solicitud.getNumeroDocumentoReceptor());
        transaccion.setNombreReceptorExterno(solicitud.getNombreReceptor());
        transaccion.setReferenciaExterna("ACH-" + UUID.randomUUID());
        transaccionRepository.save(transaccion);

        eventPublisher.publishEvent(new AuditoriaEvent(this, "TRANSFERENCIA_INTERBANCARIA_ACH",
                usuario.getIdUsuario(),
                "Orden ACH enviada a " + solicitud.getBancoDestino() +
                        " desde cuenta " + origen.getNumeroCuenta()));

        return construirRespuesta(transaccion, origen.getSaldo(),
                "Transferencia enviada a la red ACH. Estado: Pendiente de Procesamiento.");
    }

    @Override
    @Transactional
    public TransferenciaInterbancariaResponseDTO registrarRechazoAch(Long idTransaccion,
                                                                     RechazoAchSolicitudDTO solicitud) {
        Transaccion transaccion = transaccionRepository.findById(idTransaccion)
                .orElseThrow(() -> new TransaccionNoEncontradaException(idTransaccion));

        if (transaccion.getTipo() != TipoTransaccion.TRANSFERENCIA_INTERBANCARIA) {
            throw new OperacionNoPermitidaException("La transacción no corresponde a una transferencia interbancaria.");
        }

        if (transaccion.getEstado() != EstadoTransaccion.PENDIENTE_PROCESAMIENTO) {
            throw new OperacionNoPermitidaException("Solo se pueden reversar transacciones pendientes de procesamiento ACH.");
        }

        Cuenta origen = cuentaRepository.findByIdCuentaConLock(transaccion.getCuentaOrigen().getIdCuenta())
                .orElseThrow(() -> new CuentaNoEncontradaException(transaccion.getCuentaOrigen().getIdCuenta()));

        origen.setSaldo(origen.getSaldo().add(transaccion.getMonto()));
        cuentaRepository.save(origen);

        transaccion.setEstado(EstadoTransaccion.REVERSADA);
        transaccion.setMotivoRechazo(solicitud.getMotivo());
        transaccionRepository.save(transaccion);

        Transaccion reverso = new Transaccion();
        reverso.setCuentaOrigen(null);
        reverso.setCuentaDestino(origen);
        reverso.setTipo(TipoTransaccion.REVERSO_ACH);
        reverso.setMonto(transaccion.getMonto());
        reverso.setEstado(EstadoTransaccion.EXITOSA);
        reverso.setFecha(LocalDateTime.now());
        reverso.setReferenciaExterna(transaccion.getReferenciaExterna());
        reverso.setMotivoRechazo(solicitud.getMotivo());
        transaccionRepository.save(reverso);

        Long idUsuarioAuditoria = usuarioRepository
                .findByCliente_IdCliente(origen.getCliente().getIdCliente())
                .map(Usuario::getIdUsuario)
                .orElse(null);
        if (idUsuarioAuditoria != null) {
            eventPublisher.publishEvent(new AuditoriaEvent(this, "REVERSO_ACH", idUsuarioAuditoria,
                    "Reversión ACH de transacción " + idTransaccion + ". Motivo: " + solicitud.getMotivo()));
        }

        return construirRespuesta(transaccion, origen.getSaldo(),
                "La red ACH rechazó la operación. Se reversó automáticamente el valor al saldo del emisor.");
    }

    @Override
    @Transactional
    public TransferenciaInterbancariaResponseDTO registrarConfirmacionAch(Long idTransaccion,
                                                                          ConfirmacionAchSolicitudDTO solicitud) {
        Transaccion transaccion = transaccionRepository.findById(idTransaccion)
                .orElseThrow(() -> new TransaccionNoEncontradaException(idTransaccion));

        if (transaccion.getTipo() != TipoTransaccion.TRANSFERENCIA_INTERBANCARIA) {
            throw new OperacionNoPermitidaException("La transacción no corresponde a una transferencia interbancaria.");
        }

        if (transaccion.getEstado() != EstadoTransaccion.PENDIENTE_PROCESAMIENTO) {
            throw new OperacionNoPermitidaException("Solo se pueden confirmar transacciones pendientes de procesamiento ACH.");
        }

        transaccion.setEstado(EstadoTransaccion.EXITOSA);
        if (solicitud.getReferenciaConfirmacion() != null) {
            transaccion.setReferenciaExterna(solicitud.getReferenciaConfirmacion());
        }
        transaccionRepository.save(transaccion);

        Long idUsuarioAuditoria = usuarioRepository
                .findByCliente_IdCliente(transaccion.getCuentaOrigen().getCliente().getIdCliente())
                .map(Usuario::getIdUsuario)
                .orElse(null);
        if (idUsuarioAuditoria != null) {
            eventPublisher.publishEvent(new AuditoriaEvent(this, "CONFIRMACION_ACH", idUsuarioAuditoria,
                    "Confirmación ACH de transacción " + idTransaccion + ". Referencia: "
                            + transaccion.getReferenciaExterna()));
        }

        Cuenta origen = cuentaRepository.findById(transaccion.getCuentaOrigen().getIdCuenta())
                .orElseThrow(() -> new CuentaNoEncontradaException(transaccion.getCuentaOrigen().getIdCuenta()));

        return construirRespuesta(transaccion, origen.getSaldo(),
                "La red ACH confirmó la transferencia exitosamente.");
    }

    @Override
    public TransferenciaInterbancariaResponseDTO consultarTransferencia(Long idTransaccion, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        Transaccion transaccion = transaccionRepository.findById(idTransaccion)
                .orElseThrow(() -> new TransaccionNoEncontradaException(idTransaccion));

        if (transaccion.getCuentaOrigen() == null ||
                !transaccion.getCuentaOrigen().getCliente().getIdCliente()
                        .equals(usuario.getCliente().getIdCliente())) {
            throw new AccesoNoAutorizadoException();
        }

        java.math.BigDecimal saldo = cuentaRepository
                .findById(transaccion.getCuentaOrigen().getIdCuenta())
                .map(Cuenta::getSaldo)
                .orElse(java.math.BigDecimal.ZERO);

        return construirRespuesta(transaccion, saldo, "Estado ACH.");
    }

    private TransferenciaInterbancariaResponseDTO construirRespuesta(Transaccion transaccion,
                                                                    java.math.BigDecimal saldoResultante,
                                                                    String mensaje) {
        return new TransferenciaInterbancariaResponseDTO(transaccion.getIdTransaccion(),
                transaccion.getReferenciaExterna(), transaccion.getMonto(), saldoResultante,
                transaccion.getEstado().name(), transaccion.getFecha(), mensaje);
    }
}
