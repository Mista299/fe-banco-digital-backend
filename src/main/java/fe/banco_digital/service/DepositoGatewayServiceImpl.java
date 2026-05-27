package fe.banco_digital.service;

import fe.banco_digital.dto.ComprobanteDepositoDTO;
import fe.banco_digital.dto.NotificacionDepositoDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.DepositoPendiente;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.EstadoDepositoPendiente;
import fe.banco_digital.entity.EstadoMovimiento;
import fe.banco_digital.entity.Movimiento;
import fe.banco_digital.entity.TipoMovimiento;
import fe.banco_digital.exception.OperacionNoPermitidaException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.DepositoPendienteRepository;
import fe.banco_digital.repository.MovimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DepositoGatewayServiceImpl implements DepositoGatewayService {

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final DepositoPendienteRepository depositoPendienteRepository;
    private final RegistroFalloService registroFalloService;

    public DepositoGatewayServiceImpl(CuentaRepository cuentaRepository,
                                       MovimientoRepository movimientoRepository,
                                       DepositoPendienteRepository depositoPendienteRepository,
                                       RegistroFalloService registroFalloService) {
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.depositoPendienteRepository = depositoPendienteRepository;
        this.registroFalloService = registroFalloService;
    }

    @Override
    @Transactional
    public ComprobanteDepositoDTO procesarNotificacion(NotificacionDepositoDTO notificacion) {

        DepositoPendiente pendiente = depositoPendienteRepository
                .findByReferenciaGateway(notificacion.getReferenciaGateway())
                .orElseThrow(() -> new OperacionNoPermitidaException(
                        "Referencia de pago no registrada o inválida."));

        if (pendiente.getEstado() == EstadoDepositoPendiente.COMPLETADO) {
            throw new OperacionNoPermitidaException("Esta referencia de pago ya fue utilizada.");
        }

        if (pendiente.getEstado() == EstadoDepositoPendiente.EXPIRADO
                || pendiente.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            pendiente.setEstado(EstadoDepositoPendiente.EXPIRADO);
            depositoPendienteRepository.save(pendiente);
            throw new OperacionNoPermitidaException("El código de pago ha expirado.");
        }

        if (pendiente.getMonto().compareTo(notificacion.getMonto()) != 0) {
            throw new OperacionNoPermitidaException(
                    "El monto enviado (" + notificacion.getMonto() +
                    ") no coincide con el registrado (" + pendiente.getMonto() + ").");
        }

        if (!pendiente.getNumeroCuenta().equals(notificacion.getNumeroCuenta())) {
            throw new OperacionNoPermitidaException(
                    "La cuenta destino no coincide con la referencia registrada.");
        }

        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(notificacion.getNumeroCuenta())
                .orElseThrow(() -> new OperacionNoPermitidaException("Cuenta no encontrada."));

        if (cuenta.getEstado() == EstadoCuenta.BLOQUEADA) {
            registroFalloService.registrarFalloMovimiento(cuenta, TipoMovimiento.DEPOSITO, notificacion.getMonto());
            throw new OperacionNoPermitidaException("Cuenta bloqueada.");
        }

        if (cuenta.getEstado() == EstadoCuenta.INACTIVA) {
            registroFalloService.registrarFalloMovimiento(cuenta, TipoMovimiento.DEPOSITO, notificacion.getMonto());
            throw new OperacionNoPermitidaException("Cuenta cerrada.");
        }

        cuenta.setSaldo(cuenta.getSaldo().add(notificacion.getMonto()));
        cuentaRepository.save(cuenta);

        Movimiento mov = new Movimiento();
        mov.setCuenta(cuenta);
        mov.setTipo(TipoMovimiento.DEPOSITO);
        mov.setMonto(notificacion.getMonto());
        mov.setEstado(EstadoMovimiento.EXITOSO);
        mov.setFecha(LocalDateTime.now());
        mov = movimientoRepository.save(mov);

        pendiente.setEstado(EstadoDepositoPendiente.COMPLETADO);
        depositoPendienteRepository.save(pendiente);

        return new ComprobanteDepositoDTO(
                mov.getIdMovimiento(),
                mov.getFecha(),
                mov.getMonto(),
                cuenta.getNumeroCuenta(),
                cuenta.getSaldo(),
                mov.getEstado().name());
    }
}
