package fe.banco_digital.service;

import fe.banco_digital.dto.ComprobanteDepositoDTO;
import fe.banco_digital.dto.NotificacionDepositoDTO;
import fe.banco_digital.dto.RechazoDepositoDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.DepositoPendiente;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.EstadoDepositoPendiente;
import fe.banco_digital.entity.EstadoTransaccion;
import fe.banco_digital.entity.TipoTransaccion;
import fe.banco_digital.entity.Transaccion;
import fe.banco_digital.exception.DepositoRechazadoException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.DepositoPendienteRepository;
import fe.banco_digital.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DepositoGatewayServiceImpl implements DepositoGatewayService {

    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;
    private final DepositoPendienteRepository depositoPendienteRepository;
    private final RegistroFalloService registroFalloService;

    public DepositoGatewayServiceImpl(CuentaRepository cuentaRepository,
                                       TransaccionRepository transaccionRepository,
                                       DepositoPendienteRepository depositoPendienteRepository,
                                       RegistroFalloService registroFalloService) {
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
        this.depositoPendienteRepository = depositoPendienteRepository;
        this.registroFalloService = registroFalloService;
    }

    @Override
    @Transactional
    public ComprobanteDepositoDTO procesarNotificacion(NotificacionDepositoDTO notificacion) {

        // ── 1. Validar que existe un depósito pendiente para esta referencia ──
        DepositoPendiente pendiente = depositoPendienteRepository
                .findByReferenciaGateway(notificacion.getReferenciaGateway())
                .orElseThrow(() -> new DepositoRechazadoException(new RechazoDepositoDTO(
                        "Referencia de pago no registrada o inválida.",
                        notificacion.getNumeroCuenta(),
                        notificacion.getMonto(),
                        notificacion.getReferenciaGateway(),
                        notificacion.getCanalOrigen())));

        // ── 2. Verificar que no fue usada ya ──
        if (pendiente.getEstado() == EstadoDepositoPendiente.COMPLETADO) {
            throw new DepositoRechazadoException(new RechazoDepositoDTO(
                    "Esta referencia de pago ya fue utilizada.",
                    notificacion.getNumeroCuenta(),
                    notificacion.getMonto(),
                    notificacion.getReferenciaGateway(),
                    notificacion.getCanalOrigen()));
        }

        // ── 3. Verificar que no expiró ──
        if (pendiente.getEstado() == EstadoDepositoPendiente.EXPIRADO
                || pendiente.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            pendiente.setEstado(EstadoDepositoPendiente.EXPIRADO);
            depositoPendienteRepository.save(pendiente);
            throw new DepositoRechazadoException(new RechazoDepositoDTO(
                    "El código de pago ha expirado.",
                    notificacion.getNumeroCuenta(),
                    notificacion.getMonto(),
                    notificacion.getReferenciaGateway(),
                    notificacion.getCanalOrigen()));
        }

        // ── 4. Verificar que el monto coincide ──
        if (pendiente.getMonto().compareTo(notificacion.getMonto()) != 0) {
            throw new DepositoRechazadoException(new RechazoDepositoDTO(
                    "El monto enviado (" + notificacion.getMonto() + ") no coincide con el registrado (" + pendiente.getMonto() + ").",
                    notificacion.getNumeroCuenta(),
                    notificacion.getMonto(),
                    notificacion.getReferenciaGateway(),
                    notificacion.getCanalOrigen()));
        }

        // ── 5. Verificar que la cuenta destino coincide ──
        if (!pendiente.getNumeroCuenta().equals(notificacion.getNumeroCuenta())) {
            throw new DepositoRechazadoException(new RechazoDepositoDTO(
                    "La cuenta destino no coincide con la referencia registrada.",
                    notificacion.getNumeroCuenta(),
                    notificacion.getMonto(),
                    notificacion.getReferenciaGateway(),
                    notificacion.getCanalOrigen()));
        }

        // ── 6. Validar estado de la cuenta ──
        Cuenta cuenta = cuentaRepository
                .findByNumeroCuentaConLock(notificacion.getNumeroCuenta())
                .orElse(null);

        if (cuenta == null) {
            throw new DepositoRechazadoException(new RechazoDepositoDTO(
                    "Cuenta no encontrada.",
                    notificacion.getNumeroCuenta(),
                    notificacion.getMonto(),
                    notificacion.getReferenciaGateway(),
                    notificacion.getCanalOrigen()));
        }

        if (cuenta.getEstado() == EstadoCuenta.BLOQUEADA) {
            registroFalloService.registrarFallo(null, cuenta, TipoTransaccion.DEPOSITO, notificacion.getMonto());
            throw new DepositoRechazadoException(new RechazoDepositoDTO(
                    "Cuenta bloqueada.",
                    cuenta.getNumeroCuenta(),
                    notificacion.getMonto(),
                    notificacion.getReferenciaGateway(),
                    notificacion.getCanalOrigen()));
        }

        if (cuenta.getEstado() == EstadoCuenta.INACTIVA) {
            registroFalloService.registrarFallo(null, cuenta, TipoTransaccion.DEPOSITO, notificacion.getMonto());
            throw new DepositoRechazadoException(new RechazoDepositoDTO(
                    "Cuenta cerrada.",
                    cuenta.getNumeroCuenta(),
                    notificacion.getMonto(),
                    notificacion.getReferenciaGateway(),
                    notificacion.getCanalOrigen()));
        }

        // ── 7. Acreditar saldo ──
        cuenta.setSaldo(cuenta.getSaldo().add(notificacion.getMonto()));
        cuentaRepository.save(cuenta);

        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaDestino(cuenta);
        transaccion.setTipo(TipoTransaccion.DEPOSITO);
        transaccion.setMonto(notificacion.getMonto());
        transaccion.setEstado(EstadoTransaccion.EXITOSA);
        transaccion.setFecha(LocalDateTime.now());
        transaccion = transaccionRepository.save(transaccion);

        // ── 8. Marcar referencia como completada ──
        pendiente.setEstado(EstadoDepositoPendiente.COMPLETADO);
        depositoPendienteRepository.save(pendiente);

        return new ComprobanteDepositoDTO(
                transaccion.getIdTransaccion(),
                transaccion.getFecha(),
                transaccion.getMonto(),
                cuenta.getNumeroCuenta(),
                cuenta.getSaldo(),
                transaccion.getEstado().name());
    }
}
