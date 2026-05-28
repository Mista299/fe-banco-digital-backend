package fe.banco_digital.dto;

import java.math.BigDecimal;

public class SaldoTiempoRealDTO {

    private Long idCuenta;
    private BigDecimal saldoDisponible;
    private BigDecimal saldoContable;
    private String estado;
    private String tipoCuenta;

    public SaldoTiempoRealDTO(
            Long idCuenta,
            BigDecimal saldoDisponible,
            BigDecimal saldoContable,
            String estado,
            String tipoCuenta) {
        this.idCuenta = idCuenta;
        this.saldoDisponible = saldoDisponible;
        this.saldoContable = saldoContable;
        this.estado = estado;
        this.tipoCuenta = tipoCuenta;
    }

    public Long getIdCuenta() {
        return idCuenta;
    }

    public BigDecimal getSaldoDisponible() {
        return saldoDisponible;
    }

    public BigDecimal getSaldoContable() {
        return saldoContable;
    }

    public String getEstado() {
        return estado;
    }

    public String getTipoCuenta() {
        return tipoCuenta;
    }
}