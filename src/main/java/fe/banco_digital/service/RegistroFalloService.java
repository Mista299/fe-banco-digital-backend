package fe.banco_digital.service;

import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoMovimiento;
import fe.banco_digital.entity.EstadoTransferencia;
import fe.banco_digital.entity.Movimiento;
import fe.banco_digital.entity.TipoMovimiento;
import fe.banco_digital.entity.Transferencia;
import fe.banco_digital.repository.MovimientoRepository;
import fe.banco_digital.repository.TransferenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RegistroFalloService {

    private final MovimientoRepository movimientoRepository;
    private final TransferenciaRepository transferenciaRepository;

    public RegistroFalloService(MovimientoRepository movimientoRepository,
                                TransferenciaRepository transferenciaRepository) {
        this.movimientoRepository = movimientoRepository;
        this.transferenciaRepository = transferenciaRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarFalloMovimiento(Cuenta cuenta, TipoMovimiento tipo, BigDecimal monto) {
        try {
            Movimiento fallo = new Movimiento();
            fallo.setCuenta(cuenta);
            fallo.setTipo(tipo);
            fallo.setMonto(monto);
            fallo.setEstado(EstadoMovimiento.FALLIDO);
            fallo.setFecha(LocalDateTime.now());
            movimientoRepository.save(fallo);
        } catch (Exception ignorada) {
            // nunca bloquear el lanzamiento de la excepción original
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarFalloTransferencia(Cuenta origen, Cuenta destino, BigDecimal monto) {
        try {
            Transferencia fallo = new Transferencia();
            fallo.setCuentaOrigen(origen);
            fallo.setCuentaDestino(destino);
            fallo.setMonto(monto);
            fallo.setEstado(EstadoTransferencia.FALLIDA);
            fallo.setFecha(LocalDateTime.now());
            transferenciaRepository.save(fallo);
        } catch (Exception ignorada) {
            // nunca bloquear el lanzamiento de la excepción original
        }
    }
}
