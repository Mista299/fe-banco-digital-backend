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

    public MovimientoDTO aMovimientoDTO(Transaccion transaccion) {
        MovimientoDTO dto = new MovimientoDTO();

        dto.setFechaHora(transaccion.getFecha());

        // concepto
        dto.setConcepto(transaccion.getTipo().name());

        // monto con signo (🔥 importante para tu HU)
        BigDecimal monto = transaccion.getMonto();

        if (transaccion.getTipo() == TipoTransaccion.RETIRO
                || transaccion.getTipo() == TipoTransaccion.TRANSFERENCIA) {
            monto = monto.negate(); // egreso
        }

        dto.setMonto(monto);

        // ❗ NO EXISTE en tu modelo → dejamos null por ahora
        dto.setSaldoResultante(null);

        return dto;
    }

    public List<MovimientoDTO> aListaDTO(List<Transaccion> transacciones) {
        return transacciones.stream()
                .map(this::aMovimientoDTO)
                .collect(Collectors.toList());
    }
}