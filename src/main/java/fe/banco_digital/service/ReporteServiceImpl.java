package fe.banco_digital.service;

import fe.banco_digital.dto.ReporteMovimientoDTO;
import fe.banco_digital.dto.ReporteResumenDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final MovimientoRepository movimientoRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final TransferenciaExternaRepository transferenciaExternaRepository;
    private final TransferenciaInternacionalRepository transferenciaInternacionalRepository;

    public ReporteServiceImpl(
            MovimientoRepository movimientoRepository,
            TransferenciaRepository transferenciaRepository,
            TransferenciaExternaRepository transferenciaExternaRepository,
            TransferenciaInternacionalRepository transferenciaInternacionalRepository) {
        this.movimientoRepository = movimientoRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.transferenciaExternaRepository = transferenciaExternaRepository;
        this.transferenciaInternacionalRepository = transferenciaInternacionalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteResumenDTO generarReporte(LocalDateTime inicio, LocalDateTime fin) {
        List<ReporteMovimientoDTO> filas = new ArrayList<>();

        movimientoRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin)
                .forEach(m -> filas.add(desdeMov(m)));

        transferenciaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin)
                .forEach(t -> filas.add(desdeTransf(t)));

        transferenciaExternaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin)
                .forEach(t -> filas.add(desdeAch(t)));

        transferenciaInternacionalRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin)
                .forEach(t -> filas.add(desdeSwift(t)));

        filas.sort(Comparator.comparing(ReporteMovimientoDTO::getFecha).reversed());

        BigDecimal volumen = filas.stream()
                .filter(f -> "EXITOSO".equals(f.getEstado()) || "EXITOSA".equals(f.getEstado()))
                .map(ReporteMovimientoDTO::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReporteResumenDTO(filas, volumen);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportarCSV(LocalDateTime inicio, LocalDateTime fin) {
        List<ReporteMovimientoDTO> datos = generarReporte(inicio, fin).getTransacciones();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(output);

        writer.println("ID_TRANSACCION,CUENTA_ORIGEN,CUENTA_DESTINO,MONTO,ESTADO,TIPO,CANAL,FECHA");

        for (ReporteMovimientoDTO d : datos) {
            writer.println(
                    d.getIdTransaccion() + "," +
                    nullStr(d.getCuentaOrigen()) + "," +
                    nullStr(d.getCuentaDestino()) + "," +
                    d.getMonto() + "," +
                    d.getEstado() + "," +
                    d.getTipo() + "," +
                    nullStr(d.getCanal()) + "," +
                    d.getFecha()
            );
        }

        writer.flush();
        return output.toByteArray();
    }

    private ReporteMovimientoDTO desdeMov(Movimiento m) {
        ReporteMovimientoDTO dto = new ReporteMovimientoDTO();
        dto.setIdTransaccion(m.getIdMovimiento());
        dto.setMonto(m.getMonto());
        dto.setEstado(m.getEstado().name());
        dto.setTipo(m.getTipo().name());
        dto.setCanal("App");
        dto.setFecha(m.getFecha());
        if (m.getTipo() == TipoMovimiento.DEPOSITO) {
            dto.setCuentaDestino(m.getCuenta().getNumeroCuenta());
        } else {
            dto.setCuentaOrigen(m.getCuenta().getNumeroCuenta());
        }
        return dto;
    }

    private ReporteMovimientoDTO desdeTransf(Transferencia t) {
        ReporteMovimientoDTO dto = new ReporteMovimientoDTO();
        dto.setIdTransaccion(t.getIdTransferencia());
        dto.setCuentaOrigen(t.getCuentaOrigen().getNumeroCuenta());
        dto.setCuentaDestino(t.getCuentaDestino().getNumeroCuenta());
        dto.setMonto(t.getMonto());
        dto.setEstado(t.getEstado().name());
        dto.setTipo("TRANSFERENCIA");
        dto.setCanal("App");
        dto.setFecha(t.getFecha());
        return dto;
    }

    private ReporteMovimientoDTO desdeAch(TransferenciaExterna t) {
        ReporteMovimientoDTO dto = new ReporteMovimientoDTO();
        dto.setIdTransaccion(t.getIdTransfExt());
        dto.setCuentaOrigen(t.getCuentaOrigen().getNumeroCuenta());
        dto.setCuentaDestino(t.getNumeroCuentaDestino());
        dto.setMonto(t.getMonto());
        dto.setEstado(t.getEstado().name());
        dto.setTipo("TRANSFERENCIA_INTERBANCARIA");
        dto.setCanal("App");
        dto.setFecha(t.getFecha());
        return dto;
    }

    private ReporteMovimientoDTO desdeSwift(TransferenciaInternacional t) {
        ReporteMovimientoDTO dto = new ReporteMovimientoDTO();
        dto.setIdTransaccion(t.getIdTransfInt());
        dto.setCuentaOrigen(t.getCuentaOrigen().getNumeroCuenta());
        dto.setCuentaDestino(t.getIbanCuentaDestino());
        dto.setMonto(t.getMontoCop());
        dto.setEstado(t.getEstado().name());
        dto.setTipo("TRANSFERENCIA_INTERNACIONAL");
        dto.setCanal("App");
        dto.setFecha(t.getFecha());
        return dto;
    }

    private String nullStr(Object o) {
        return o == null ? "" : o.toString();
    }
}
