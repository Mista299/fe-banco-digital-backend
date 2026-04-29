package fe.banco_digital.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransaccionRespuestaDTO {

    private Long idTransaccion;
    private String tipo;
    private BigDecimal monto;
    private BigDecimal saldoResultante;
    private String estado;
    private LocalDateTime fecha;
    private String mensaje;

    public TransaccionRespuestaDTO(Long idTransaccion, String tipo, BigDecimal monto,
                                    BigDecimal saldoResultante, String estado,
                                    LocalDateTime fecha, String mensaje) {
        this.idTransaccion = idTransaccion;
        this.tipo = tipo;
        this.monto = monto;
        this.saldoResultante = saldoResultante;
        this.estado = estado;
        this.fecha = fecha;
        this.mensaje = mensaje;
    }

    public Long getIdTransaccion() { return idTransaccion; }
    public String getTipo() { return tipo; }
    public BigDecimal getMonto() { return monto; }
    public BigDecimal getSaldoResultante() { return saldoResultante; }
    public String getEstado() { return estado; }
    public LocalDateTime getFecha() { return fecha; }
    public String getMensaje() { return mensaje; }
}
