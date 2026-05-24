package fe.banco_digital.dto;

import java.math.BigDecimal;

public class ReporteEstadoCuentaDTO {

    private String titular;
    private String numeroCuenta;
    private BigDecimal saldo;
    private String estado;

    public ReporteEstadoCuentaDTO(
            String titular,
            String numeroCuenta,
            BigDecimal saldo,
            String estado) {
        this.titular = titular;
        this.numeroCuenta = numeroCuenta;
        this.saldo = saldo;
        this.estado = estado;
    }

    public String getTitular() {
        return titular;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public String getEstado() {
        return estado;
    }
}