package fe.banco_digital.service;

import fe.banco_digital.dto.ValidacionTransaccionResponseDTO;
import fe.banco_digital.dto.ValidacionTransaccionSolicitudDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.event.AuditoriaEvent;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class MotorValidacionServiceImpl implements MotorValidacionService {

    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public MotorValidacionServiceImpl(UsuarioRepository usuarioRepository,
                                      CuentaRepository cuentaRepository,
                                      ApplicationEventPublisher eventPublisher) {
        this.usuarioRepository = usuarioRepository;
        this.cuentaRepository = cuentaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public ValidacionTransaccionResponseDTO validar(ValidacionTransaccionSolicitudDTO solicitud, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        Cuenta cuenta = cuentaRepository.findByIdCuentaAndCliente_IdCliente(
                        solicitud.getIdCuentaOrigen(), usuario.getCliente().getIdCliente())
                .orElseThrow(AccesoNoAutorizadoException::new);

        ValidacionTransaccionResponseDTO resultado = validarCuentaParaDebito(cuenta, solicitud.getMonto());
        if (!resultado.isAutorizada()) {
            eventPublisher.publishEvent(new AuditoriaEvent(this, resultado.getCodigo(),
                    usuario.getIdUsuario(), resultado.getMensaje()));
        }
        return resultado;
    }

    @Override
    public ValidacionTransaccionResponseDTO validarCuentaParaDebito(Cuenta cuenta, BigDecimal monto) {
        if (cuenta == null) {
            throw new CuentaNoEncontradaException(null);
        }

        if (cuenta.getEstado() == EstadoCuenta.BLOQUEADA) {
            registrarFalloValidacion(cuenta, "VALIDACION_BLOQUEADA",
                    "Intento de operación desde cuenta bloqueada " + cuenta.getNumeroCuenta());
            return rechazada("CUENTA_BLOQUEADA",
                    "Operación no permitida. Por razones de seguridad, su cuenta presenta una restricción activa.",
                    cuenta);
        }

        if (cuenta.getEstado() == EstadoCuenta.INACTIVA) {
            registrarFalloValidacion(cuenta, "VALIDACION_CUENTA_INACTIVA",
                    "Intento de operación desde cuenta inactiva " + cuenta.getNumeroCuenta());
            return rechazada("CUENTA_INACTIVA",
                    "La cuenta de origen no se encuentra habilitada para realizar transacciones.",
                    cuenta);
        }

        if (cuenta.getSaldo() == null || cuenta.getSaldo().compareTo(monto) < 0) {
            registrarFalloValidacion(cuenta, "TRANSACCION_FALLIDA_POR_FONDOS",
                    "Transacción fallida por fondos en cuenta " + cuenta.getNumeroCuenta());
            return rechazada("SALDO_INSUFICIENTE",
                    "Saldo insuficiente para completar esta operación.",
                    cuenta);
        }

        return new ValidacionTransaccionResponseDTO(true, "AUTORIZADA",
                "Transacción autorizada. La cuenta está activa y tiene saldo suficiente.",
                cuenta.getIdCuenta(), cuenta.getEstado().name(), cuenta.getSaldo());
    }

    private ValidacionTransaccionResponseDTO rechazada(String codigo, String mensaje, Cuenta cuenta) {
        return new ValidacionTransaccionResponseDTO(false, codigo, mensaje,
                cuenta.getIdCuenta(), cuenta.getEstado().name(), cuenta.getSaldo());
    }

    private void registrarFalloValidacion(Cuenta cuenta, String tipoEvento, String descripcion) {
        // La publicación en auditoría se hace en validar(...), donde sí conocemos
        // el usuario autenticado. Este método conserva el punto de extensión de reglas.
    }
}
