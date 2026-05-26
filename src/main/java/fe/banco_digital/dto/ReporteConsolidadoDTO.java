package fe.banco_digital.dto;

import java.math.BigDecimal;

public class ReporteConsolidadoDTO {

    private BigDecimal totalSistema;
    private BigDecimal totalAhorros;
    private BigDecimal totalCorriente;

    public ReporteConsolidadoDTO(
            BigDecimal totalSistema,
            BigDecimal totalAhorros,
            BigDecimal totalCorriente) {
        this.totalSistema = totalSistema;
        this.totalAhorros = totalAhorros;
        this.totalCorriente = totalCorriente;
    }

    public BigDecimal getTotalSistema() {
        return totalSistema;
    }

    public BigDecimal getTotalAhorros() {
        return totalAhorros;
    }

    public BigDecimal getTotalCorriente() {
        return totalCorriente;
    }
}