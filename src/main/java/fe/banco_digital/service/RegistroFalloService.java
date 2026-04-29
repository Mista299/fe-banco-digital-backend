package fe.banco_digital.service;

import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoTransaccion;
import fe.banco_digital.entity.TipoTransaccion;
import fe.banco_digital.entity.Transaccion;
import fe.banco_digital.repository.TransaccionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RegistroFalloService {

    private final TransaccionRepository transaccionRepository;

    public RegistroFalloService(TransaccionRepository transaccionRepository) {
        this.transaccionRepository = transaccionRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarFallo(Cuenta origen, Cuenta destino,
                               TipoTransaccion tipo, BigDecimal monto) {
        try {
            Transaccion fallo = new Transaccion();
            fallo.setCuentaOrigen(origen);
            fallo.setCuentaDestino(destino);
            fallo.setTipo(tipo);
            fallo.setMonto(monto);
            fallo.setEstado(EstadoTransaccion.FALLIDA);
            fallo.setFecha(LocalDateTime.now());
            transaccionRepository.save(fallo);
        } catch (Exception ignorada) {
            // nunca bloquear el lanzamiento de la excepción original
        }
    }
}
