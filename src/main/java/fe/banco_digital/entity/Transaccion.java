package fe.banco_digital.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaccion {

    private Long idTransaccion;
    private Cuenta cuentaOrigen;
    private Cuenta cuentaDestino;
    private BigDecimal monto;
    private LocalDateTime fecha = LocalDateTime.now();
    private TipoTransaccion tipo;
    private EstadoTransaccion estado;
    private String canal;

    public Long getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(Long idTransaccion) { this.idTransaccion = idTransaccion; }
    public Cuenta getCuentaOrigen() { return cuentaOrigen; }
    public void setCuentaOrigen(Cuenta cuentaOrigen) { this.cuentaOrigen = cuentaOrigen; }
    public Cuenta getCuentaDestino() { return cuentaDestino; }
    public void setCuentaDestino(Cuenta cuentaDestino) { this.cuentaDestino = cuentaDestino; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public TipoTransaccion getTipo() { return tipo; }
    public void setTipo(TipoTransaccion tipo) { this.tipo = tipo; }
    public EstadoTransaccion getEstado() { return estado; }
    public void setEstado(EstadoTransaccion estado) { this.estado = estado; }
    public String getCanal() { return canal; }
    public void setCanal(String canal) { this.canal = canal; }
}
