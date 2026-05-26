package fe.banco_digital.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReporteMovimientoDTO {

    private Long idTransaccion;

    private Long cuentaOrigen;

    private Long cuentaDestino;

    private BigDecimal monto;

    private String estado;

    private String tipo;

    private String canal;

    private LocalDateTime fecha;

    // GETTERS Y SETTERS

    public Long getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(Long idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public Long getCuentaOrigen() {
        return cuentaOrigen;
    }

    public void setCuentaOrigen(Long cuentaOrigen) {
        this.cuentaOrigen = cuentaOrigen;
    }

    public Long getCuentaDestino() {
        return cuentaDestino;
    }

    public void setCuentaDestino(Long cuentaDestino) {
        this.cuentaDestino = cuentaDestino;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // ESTE ES EL IMPORTANTE
    public String getTipo() {
        return tipo;
    }

    // ESTE ES EL IMPORTANTE
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}