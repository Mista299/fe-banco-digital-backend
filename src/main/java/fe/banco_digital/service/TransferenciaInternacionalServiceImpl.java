package fe.banco_digital.service;

import fe.banco_digital.dto.ConfirmacionSwiftSolicitudDTO;
import fe.banco_digital.dto.RechazoSwiftSolicitudDTO;
import fe.banco_digital.dto.TransferenciaInternacionalResponseDTO;
import fe.banco_digital.dto.TransferenciaInternacionalSolicitudDTO;
import fe.banco_digital.dto.ValidacionTransaccionResponseDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoMovimiento;
import fe.banco_digital.entity.EstadoTransferenciaInternacional;
import fe.banco_digital.entity.Movimiento;
import fe.banco_digital.entity.TipoMovimiento;
import fe.banco_digital.entity.TransferenciaInternacional;
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
import fe.banco_digital.repository.TransferenciaInternacionalRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferenciaInternacionalServiceImpl implements TransferenciaInternacionalService {

    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final TransferenciaInternacionalRepository transferenciaInternacionalRepository;
    private final MovimientoRepository movimientoRepository;
    private final MotorValidacionService motorValidacionService;
    private final RegistroFalloService registroFalloService;
    private final ApplicationEventPublisher eventPublisher;

    public TransferenciaInternacionalServiceImpl(UsuarioRepository usuarioRepository,
                                                  CuentaRepository cuentaRepository,
                                                  TransferenciaInternacionalRepository transferenciaInternacionalRepository,
                                                  MovimientoRepository movimientoRepository,
                                                  MotorValidacionService motorValidacionService,
                                                  RegistroFalloService registroFalloService,
                                                  ApplicationEventPublisher eventPublisher) {
        this.usuarioRepository = usuarioRepository;
        this.cuentaRepository = cuentaRepository;
        this.transferenciaInternacionalRepository = transferenciaInternacionalRepository;
        this.movimientoRepository = movimientoRepository;
        this.motorValidacionService = motorValidacionService;
        this.registroFalloService = registroFalloService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public TransferenciaInternacionalResponseDTO iniciarTransferencia(
            TransferenciaInternacionalSolicitudDTO solicitud, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        cuentaRepository.findByIdCuentaAndCliente_IdCliente(solicitud.getIdCuentaOrigen(),
                        usuario.getCliente().getIdCliente())
                .orElseThrow(AccesoNoAutorizadoException::new);

        Cuenta origen = cuentaRepository.findByIdCuentaConLock(solicitud.getIdCuentaOrigen())
                .orElseThrow(() -> new CuentaNoEncontradaException(solicitud.getIdCuentaOrigen()));

        BigDecimal montoCop = solicitud.getMontoUsd()
                .multiply(solicitud.getTasaCambio())
                .setScale(4, RoundingMode.HALF_UP);

        ValidacionTransaccionResponseDTO validacion =
                motorValidacionService.validarCuentaParaDebito(origen, montoCop);

        if (!validacion.isAutorizada()) {
            registroFalloService.registrarFalloMovimiento(origen, TipoMovimiento.RETIRO, montoCop);
            if ("SALDO_INSUFICIENTE".equals(validacion.getCodigo())) {
                throw new SaldoInsuficienteException();
            }
            throw new OperacionNoPermitidaException(validacion.getMensaje());
        }

        origen.setSaldo(origen.getSaldo().subtract(montoCop));
        cuentaRepository.save(origen);

        TransferenciaInternacional ti = new TransferenciaInternacional();
        ti.setCuentaOrigen(origen);
        ti.setBancoDestino(solicitud.getBancoDestino());
        ti.setCodigoSwift(solicitud.getCodigoSwift());
        ti.setPaisDestino(solicitud.getPaisDestino());
        ti.setTipoCuentaDestino(solicitud.getTipoCuentaDestino());
        ti.setIbanCuentaDestino(solicitud.getIbanCuentaDestino());
        ti.setTipoDocumentoReceptor(solicitud.getTipoDocumentoReceptor());
        ti.setNumeroDocumentoReceptor(solicitud.getNumeroDocumentoReceptor());
        ti.setNombreReceptor(solicitud.getNombreReceptor());
        ti.setMontoUsd(solicitud.getMontoUsd());
        ti.setTasaCambio(solicitud.getTasaCambio());
        ti.setMontoCop(montoCop);
        ti.setMoneda(solicitud.getMoneda() != null ? solicitud.getMoneda() : "USD");
        ti.setEstado(EstadoTransferenciaInternacional.PENDIENTE_PROCESAMIENTO);
        ti.setFecha(LocalDateTime.now());
        ti.setReferenciaSwift("SWIFT-" + UUID.randomUUID());
        transferenciaInternacionalRepository.save(ti);

        eventPublisher.publishEvent(new AuditoriaEvent(this, "TRANSFERENCIA_INTERNACIONAL_SWIFT",
                usuario.getIdUsuario(),
                "Orden SWIFT enviada a " + sanitizar(solicitud.getBancoDestino())
                        + " (" + sanitizar(solicitud.getPaisDestino()) + ")"
                        + " por " + sanitizar(solicitud.getMontoUsd().toPlainString()) + " " + sanitizar(ti.getMoneda())
                        + " desde cuenta " + sanitizar(origen.getNumeroCuenta())));

        return construirRespuesta(ti, origen.getSaldo(),
                "Transferencia enviada a la red SWIFT. Estado: Pendiente de Procesamiento.");
    }

    @Override
    @Transactional
    public TransferenciaInternacionalResponseDTO registrarRechazoSwift(Long idTransfInt,
                                                                        RechazoSwiftSolicitudDTO solicitud) {
        TransferenciaInternacional ti = transferenciaInternacionalRepository.findById(idTransfInt)
                .orElseThrow(() -> new TransaccionNoEncontradaException(idTransfInt));

        if (ti.getEstado() != EstadoTransferenciaInternacional.PENDIENTE_PROCESAMIENTO) {
            throw new OperacionNoPermitidaException("Solo se pueden reversar transferencias pendientes de procesamiento SWIFT.");
        }

        Cuenta origen = cuentaRepository.findByIdCuentaConLock(ti.getCuentaOrigen().getIdCuenta())
                .orElseThrow(() -> new CuentaNoEncontradaException(ti.getCuentaOrigen().getIdCuenta()));

        origen.setSaldo(origen.getSaldo().add(ti.getMontoCop()));
        cuentaRepository.save(origen);

        ti.setEstado(EstadoTransferenciaInternacional.REVERSADA);
        ti.setMotivoRechazo(solicitud.getMotivo());
        transferenciaInternacionalRepository.save(ti);

        Movimiento devolucion = new Movimiento();
        devolucion.setCuenta(origen);
        devolucion.setTipo(TipoMovimiento.DEPOSITO);
        devolucion.setMonto(ti.getMontoCop());
        devolucion.setEstado(EstadoMovimiento.EXITOSO);
        devolucion.setFecha(LocalDateTime.now());
        devolucion.setDescripcion("Reversión SWIFT: " + ti.getReferenciaSwift());
        movimientoRepository.save(devolucion);

        Long idUsuarioAuditoria = usuarioRepository.findByCliente_IdCliente(origen.getCliente().getIdCliente())
                .map(Usuario::getIdUsuario)
                .orElse(null);
        if (idUsuarioAuditoria != null) {
            eventPublisher.publishEvent(new AuditoriaEvent(this, "RECHAZO_SWIFT", idUsuarioAuditoria,
                    "Rechazo SWIFT de transferencia " + idTransfInt + ". Motivo: " + sanitizar(solicitud.getMotivo())));
        }

        return construirRespuesta(ti, origen.getSaldo(),
                "La red SWIFT rechazó la operación. Se reversó automáticamente el valor al saldo del emisor.");
    }

    @Override
    @Transactional
    public TransferenciaInternacionalResponseDTO registrarConfirmacionSwift(Long idTransfInt,
                                                                             ConfirmacionSwiftSolicitudDTO solicitud) {
        TransferenciaInternacional ti = transferenciaInternacionalRepository.findById(idTransfInt)
                .orElseThrow(() -> new TransaccionNoEncontradaException(idTransfInt));

        if (ti.getEstado() != EstadoTransferenciaInternacional.PENDIENTE_PROCESAMIENTO) {
            throw new OperacionNoPermitidaException("Solo se pueden confirmar transferencias pendientes de procesamiento SWIFT.");
        }

        ti.setEstado(EstadoTransferenciaInternacional.EXITOSA);
        if (solicitud.getReferenciaConfirmacion() != null) {
            ti.setReferenciaSwift(solicitud.getReferenciaConfirmacion());
        }
        transferenciaInternacionalRepository.save(ti);

        Long idUsuarioAuditoria = usuarioRepository.findByCliente_IdCliente(ti.getCuentaOrigen().getCliente().getIdCliente())
                .map(Usuario::getIdUsuario)
                .orElse(null);
        if (idUsuarioAuditoria != null) {
            eventPublisher.publishEvent(new AuditoriaEvent(this, "CONFIRMACION_SWIFT", idUsuarioAuditoria,
                    "Confirmación SWIFT de transferencia " + idTransfInt
                            + ". Referencia: " + ti.getReferenciaSwift()));
        }

        Cuenta origen = cuentaRepository.findById(ti.getCuentaOrigen().getIdCuenta())
                .orElseThrow(() -> new CuentaNoEncontradaException(ti.getCuentaOrigen().getIdCuenta()));

        return construirRespuesta(ti, origen.getSaldo(),
                "La red SWIFT confirmó la transferencia exitosamente.");
    }

    @Override
    @Transactional(readOnly = true)
    public TransferenciaInternacionalResponseDTO consultarTransferencia(Long idTransfInt, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        TransferenciaInternacional ti = transferenciaInternacionalRepository.findById(idTransfInt)
                .orElseThrow(() -> new TransaccionNoEncontradaException(idTransfInt));

        if (!ti.getCuentaOrigen().getCliente().getIdCliente()
                .equals(usuario.getCliente().getIdCliente())) {
            throw new AccesoNoAutorizadoException();
        }

        BigDecimal saldo = cuentaRepository
                .findById(ti.getCuentaOrigen().getIdCuenta())
                .map(Cuenta::getSaldo)
                .orElse(BigDecimal.ZERO);

        return construirRespuesta(ti, saldo, "Estado SWIFT.");
    }

    private TransferenciaInternacionalResponseDTO construirRespuesta(TransferenciaInternacional ti,
                                                                      BigDecimal saldoResultante,
                                                                      String mensaje) {
        return new TransferenciaInternacionalResponseDTO(
                ti.getIdTransfInt(), ti.getReferenciaSwift(),
                ti.getMontoUsd(), ti.getMontoCop(), ti.getTasaCambio(),
                saldoResultante, ti.getEstado().name(), ti.getFecha(), mensaje);
    }

    private String sanitizar(String valor) {
        return valor == null ? "" : valor.replaceAll("[\r\n\t]", "_");
    }
}
