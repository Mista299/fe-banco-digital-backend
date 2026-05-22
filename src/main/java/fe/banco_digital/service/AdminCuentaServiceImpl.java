package fe.banco_digital.service;

import fe.banco_digital.dto.DecisionAperturaRespuestaDTO;
import fe.banco_digital.dto.SolicitudPendienteDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.event.AuditoriaEvent;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.exception.OperacionNoPermitidaException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminCuentaServiceImpl implements AdminCuentaService {

    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AdminCuentaServiceImpl(CuentaRepository cuentaRepository,
                                  UsuarioRepository usuarioRepository,
                                  ApplicationEventPublisher eventPublisher) {
        this.cuentaRepository = cuentaRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SolicitudPendienteDTO> listarPendientes() {
        return cuentaRepository.findByEstadoConCliente(EstadoCuenta.PENDIENTE_APROBACION)
                .stream()
                .map(SolicitudPendienteDTO::new)
                .toList();
    }

    @Override
    @Transactional
    public DecisionAperturaRespuestaDTO aprobarApertura(Long idCuenta, String adminUsername) {
        Usuario admin = usuarioRepository.findByUsername(adminUsername)
                .orElseThrow(AutenticacionFallidaException::new);

        Cuenta cuenta = cuentaRepository.findById(idCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(idCuenta));

        if (cuenta.getEstado() != EstadoCuenta.PENDIENTE_APROBACION) {
            throw new OperacionNoPermitidaException("La cuenta no tiene una solicitud de apertura pendiente.");
        }

        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuentaRepository.save(cuenta);

        eventPublisher.publishEvent(new AuditoriaEvent(this, "APROBACION_APERTURA_CUENTA",
                admin.getIdUsuario(),
                "Cuenta " + cuenta.getNumeroCuenta() + " aprobada y activada por el administrador."));

        return new DecisionAperturaRespuestaDTO(
                cuenta.getIdCuenta(),
                cuenta.getNumeroCuenta(),
                cuenta.getEstado().name(),
                "Cuenta aprobada exitosamente.");
    }

    @Override
    @Transactional
    public DecisionAperturaRespuestaDTO rechazarApertura(Long idCuenta, String adminUsername) {
        Usuario admin = usuarioRepository.findByUsername(adminUsername)
                .orElseThrow(AutenticacionFallidaException::new);

        Cuenta cuenta = cuentaRepository.findById(idCuenta)
                .orElseThrow(() -> new CuentaNoEncontradaException(idCuenta));

        if (cuenta.getEstado() != EstadoCuenta.PENDIENTE_APROBACION) {
            throw new OperacionNoPermitidaException("La cuenta no tiene una solicitud de apertura pendiente.");
        }

        cuenta.setEstado(EstadoCuenta.INACTIVA);
        cuentaRepository.save(cuenta);

        eventPublisher.publishEvent(new AuditoriaEvent(this, "RECHAZO_APERTURA_CUENTA",
                admin.getIdUsuario(),
                "Solicitud de apertura para cuenta " + cuenta.getNumeroCuenta() + " rechazada por el administrador."));

        return new DecisionAperturaRespuestaDTO(
                cuenta.getIdCuenta(),
                cuenta.getNumeroCuenta(),
                cuenta.getEstado().name(),
                "Solicitud de apertura rechazada.");
    }
}
