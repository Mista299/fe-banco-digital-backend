package fe.banco_digital.dto;

import java.math.BigDecimal;
import java.util.List;

public class ReporteResumenDTO {

    private List<ReporteMovimientoDTO> transacciones;
    private long totalTransacciones;
    private BigDecimal volumenTotal;

    public ReporteResumenDTO(List<ReporteMovimientoDTO> transacciones, BigDecimal volumenTotal) {
        this.transacciones = transacciones;
        this.totalTransacciones = transacciones.size();
        this.volumenTotal = volumenTotal;
    }

    public List<ReporteMovimientoDTO> getTransacciones() { return transacciones; }
    public long getTotalTransacciones() { return totalTransacciones; }
    public BigDecimal getVolumenTotal() { return volumenTotal; }
}
