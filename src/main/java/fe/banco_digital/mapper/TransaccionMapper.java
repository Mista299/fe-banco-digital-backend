package fe.banco_digital.mapper;

import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.entity.Transaccion;
import fe.banco_digital.entity.TipoTransaccion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransaccionMapper {

    public MovimientoDTO aMovimientoDTO(Transaccion transaccion, Long idCuenta) {
        MovimientoDTO dto = new MovimientoDTO();

        dto.setFechaHora(transaccion.getFecha());
        dto.setConcepto(transaccion.getTipo().name());

        BigDecimal monto = transaccion.getMonto();

        boolean esOrigen = transaccion.getCuentaOrigen() != null
                && transaccion.getCuentaOrigen().getIdCuenta().equals(idCuenta);

        if (esOrigen) {
            monto = monto.negate(); // egreso
        }
        // si es destino (depósito recibido o transferencia recibida) → positivo

        dto.setMonto(monto);
        dto.setSaldoResultante(null);

        return dto;
    }

    public List<MovimientoDTO> aListaDTO(List<Transaccion> transacciones, Long idCuenta) {
        return transacciones.stream()
                .map(t -> aMovimientoDTO(t, idCuenta))
                .collect(Collectors.toList());
    }
}