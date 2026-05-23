package fe.banco_digital.mapper;

import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.entity.Movimiento;
import fe.banco_digital.entity.TipoMovimiento;
import fe.banco_digital.entity.Transferencia;
import fe.banco_digital.entity.TransferenciaExterna;
import fe.banco_digital.entity.TransferenciaInternacional;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransaccionMapper {

    public MovimientoDTO aMovimientoDTO(Movimiento m) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setIdTransaccion(m.getIdMovimiento());
        dto.setFechaHora(m.getFecha());
        dto.setConcepto(m.getTipo().name());
        dto.setEstado(m.getEstado() != null ? m.getEstado().name() : null);
        BigDecimal monto = m.getTipo() == TipoMovimiento.RETIRO
                ? m.getMonto().negate()
                : m.getMonto();
        dto.setMonto(monto);
        return dto;
    }

    public MovimientoDTO aMovimientoDTO(Transferencia t, Long idCuenta) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setIdTransaccion(t.getIdTransferencia());
        dto.setFechaHora(t.getFecha());
        dto.setConcepto(t.getEstado() != null ? "TRANSFERENCIA" : "TRANSFERENCIA");
        dto.setEstado(t.getEstado() != null ? t.getEstado().name() : null);
        boolean esOrigen = t.getCuentaOrigen() != null
                && idCuenta.equals(t.getCuentaOrigen().getIdCuenta());
        dto.setMonto(esOrigen ? t.getMonto().negate() : t.getMonto());
        return dto;
    }

    public MovimientoDTO aMovimientoDTO(TransferenciaExterna te) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setIdTransaccion(te.getIdTransfExt());
        dto.setFechaHora(te.getFecha());
        dto.setConcepto("TRANSFERENCIA_INTERBANCARIA");
        dto.setEstado(te.getEstado() != null ? te.getEstado().name() : null);
        dto.setMonto(te.getMonto().negate());
        dto.setBancoDestino(te.getBancoDestino());
        dto.setNombreReceptorExterno(te.getNombreReceptor());
        return dto;
    }

    public MovimientoDTO aMovimientoDTO(TransferenciaInternacional ti) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setIdTransaccion(ti.getIdTransfInt());
        dto.setFechaHora(ti.getFecha());
        dto.setConcepto("TRANSFERENCIA_INTERNACIONAL");
        dto.setEstado(ti.getEstado() != null ? ti.getEstado().name() : null);
        dto.setMonto(ti.getMontoCop().negate());
        dto.setBancoDestino(ti.getBancoDestino() + " · " + ti.getPaisDestino());
        dto.setNombreReceptorExterno(ti.getNombreReceptor());
        return dto;
    }

    public List<MovimientoDTO> aListaDTOUnificada(
            List<Movimiento> movimientos,
            List<Transferencia> transferencias, Long idCuenta,
            List<TransferenciaExterna> externas,
            List<TransferenciaInternacional> internacionales) {

        List<MovimientoDTO> resultado = new ArrayList<>();

        movimientos.forEach(m -> resultado.add(aMovimientoDTO(m)));
        transferencias.forEach(t -> resultado.add(aMovimientoDTO(t, idCuenta)));
        externas.forEach(te -> resultado.add(aMovimientoDTO(te)));
        internacionales.forEach(ti -> resultado.add(aMovimientoDTO(ti)));

        resultado.sort(Comparator.comparing(MovimientoDTO::getFechaHora).reversed());
        return resultado;
    }
}
