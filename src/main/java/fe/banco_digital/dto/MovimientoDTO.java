package fe.banco_digital.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimientoDTO {

    private Long idTransaccion;
    private LocalDateTime fechaHora;
    private String concepto;
    private BigDecimal monto;
    private BigDecimal saldoResultante;
    private String bancoDestino;
    private String nombreReceptorExterno;
    private String estado;

    public MovimientoDTO() {
    }

    public Long getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(Long idTransaccion) { this.idTransaccion = idTransaccion; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public BigDecimal getSaldoResultante() { return saldoResultante; }
    public void setSaldoResultante(BigDecimal saldoResultante) { this.saldoResultante = saldoResultante; }

    public String getBancoDestino() { return bancoDestino; }
    public void setBancoDestino(String bancoDestino) { this.bancoDestino = bancoDestino; }

    public String getNombreReceptorExterno() { return nombreReceptorExterno; }
    public void setNombreReceptorExterno(String nombreReceptorExterno) { this.nombreReceptorExterno = nombreReceptorExterno; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}