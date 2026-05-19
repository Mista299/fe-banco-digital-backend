package fe.banco_digital.mapper;

import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.entity.Transaccion;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransaccionMapper {

    public MovimientoDTO aMovimientoDTO(Transaccion transaccion, Long idCuenta) {
        MovimientoDTO dto = new MovimientoDTO();

        dto.setIdTransaccion(transaccion.getIdTransaccion());
        dto.setFechaHora(transaccion.getFecha());
        dto.setConcepto(transaccion.getTipo().name());
        dto.setEstado(transaccion.getEstado() != null ? transaccion.getEstado().name() : null);
        dto.setBancoDestino(transaccion.getBancoDestino());
        dto.setNombreReceptorExterno(transaccion.getNombreReceptorExterno());

        BigDecimal monto = transaccion.getMonto();

        // idCuentaOrigen solo lo puebla JPA al cargar desde BD; cuando la entidad
        // se construye manualmente (tests) hay que leerlo desde la relación.
        Long idOrigen = transaccion.getIdCuentaOrigen() != null
                ? transaccion.getIdCuentaOrigen()
                : (transaccion.getCuentaOrigen() != null ? transaccion.getCuentaOrigen().getIdCuenta() : null);

        if (idCuenta.equals(idOrigen)) {
            monto = monto.negate();
        }

        dto.setMonto(monto);

        return dto;
    }

    public List<MovimientoDTO> aListaDTO(List<Transaccion> transacciones, Long idCuenta) {
        return transacciones.stream()
                .map(t -> aMovimientoDTO(t, idCuenta))
                .collect(Collectors.toList());
    }
}