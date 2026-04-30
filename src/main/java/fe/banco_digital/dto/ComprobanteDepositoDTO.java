package fe.banco_digital.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ComprobanteDepositoDTO {

    private Long numeroOperacion;
    private LocalDateTime fechaHora;
    private BigDecimal monto;
    private String numeroCuentaDestino;
    private BigDecimal saldoResultante;
    private String estado;

    public ComprobanteDepositoDTO(Long numeroOperacion, LocalDateTime fechaHora,
                                   BigDecimal monto, String numeroCuentaDestino,
                                   BigDecimal saldoResultante, String estado) {
        this.numeroOperacion = numeroOperacion;
        this.fechaHora = fechaHora;
        this.monto = monto;
        this.numeroCuentaDestino = numeroCuentaDestino;
        this.saldoResultante = saldoResultante;
        this.estado = estado;
    }

    public Long getNumeroOperacion() { return numeroOperacion; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public BigDecimal getMonto() { return monto; }
    public String getNumeroCuentaDestino() { return numeroCuentaDestino; }
    public BigDecimal getSaldoResultante() { return saldoResultante; }
    public String getEstado() { return estado; }
}
