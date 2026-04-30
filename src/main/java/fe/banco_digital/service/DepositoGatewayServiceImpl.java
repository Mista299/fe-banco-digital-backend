package fe.banco_digital.service;

import fe.banco_digital.dto.ComprobanteDepositoDTO;
import fe.banco_digital.dto.NotificacionDepositoDTO;
import fe.banco_digital.dto.RechazoDepositoDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.EstadoTransaccion;
import fe.banco_digital.entity.TipoTransaccion;
import fe.banco_digital.entity.Transaccion;
import fe.banco_digital.exception.DepositoRechazadoException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class DepositoGatewayServiceImpl implements DepositoGatewayService {

    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;
    private final RegistroFalloService registroFalloService;

    public DepositoGatewayServiceImpl(CuentaRepository cuentaRepository,
                                       TransaccionRepository transaccionRepository,
                                       RegistroFalloService registroFalloService) {
        this.cuentaRepository = cuentaRepository;
        this.transaccionRepository = transaccionRepository;
        this.registroFalloService = registroFalloService;
    }

    @Override
    @Transactional
    public ComprobanteDepositoDTO procesarNotificacion(NotificacionDepositoDTO notificacion) {
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

        cuenta.setSaldo(cuenta.getSaldo().add(notificacion.getMonto()));
        cuentaRepository.save(cuenta);

        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaDestino(cuenta);
        transaccion.setTipo(TipoTransaccion.DEPOSITO);
        transaccion.setMonto(notificacion.getMonto());
        transaccion.setEstado(EstadoTransaccion.EXITOSA);
        transaccion.setFecha(LocalDateTime.now());
        transaccion = transaccionRepository.save(transaccion);

        return new ComprobanteDepositoDTO(
                transaccion.getIdTransaccion(),
                transaccion.getFecha(),
                transaccion.getMonto(),
                cuenta.getNumeroCuenta(),
                cuenta.getSaldo(),
                transaccion.getEstado().name());
    }
}
