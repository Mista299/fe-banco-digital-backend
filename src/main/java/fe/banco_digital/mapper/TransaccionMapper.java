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
        dto.setConcepto(generarConcepto(transaccion));

        BigDecimal monto = transaccion.getMonto();

        boolean esOrigen = transaccion.getCuentaOrigen() != null
                && transaccion.getCuentaOrigen().getIdCuenta().equals(idCuenta);

        if (esOrigen) {
            dto.setTipo("EGRESO");
            dto.setSigno("-");
            monto = monto.negate();
        } else {
            dto.setTipo("INGRESO");
            dto.setSigno("+");
        }

        dto.setMonto(monto);
        dto.setSaldoResultante(null);

        return dto;
    }

    public List<MovimientoDTO> aListaDTO(List<Transaccion> transacciones, Long idCuenta) {
        return transacciones.stream()
                .map(t -> aMovimientoDTO(t, idCuenta))
                .collect(Collectors.toList());
    }

    private String generarConcepto(Transaccion t) {
        switch (t.getTipo()) {
            case DEPOSITO:
                return "Consignación";
            case RETIRO:
                return "Retiro Cajero";
            case TRANSFERENCIA:
                return "Transferencia";
            default:
                return "Movimiento";
        }
    }
}