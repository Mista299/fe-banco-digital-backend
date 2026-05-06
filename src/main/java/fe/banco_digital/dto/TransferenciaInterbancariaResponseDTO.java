package fe.banco_digital.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferenciaInterbancariaResponseDTO {

    private Long idTransaccion;
    private String referenciaExterna;
    private BigDecimal monto;
    private BigDecimal saldoResultante;
    private String estado;
    private LocalDateTime fecha;
    private String mensaje;

    public TransferenciaInterbancariaResponseDTO(Long idTransaccion, String referenciaExterna,
                                                BigDecimal monto, BigDecimal saldoResultante,
                                                String estado, LocalDateTime fecha, String mensaje) {
        this.idTransaccion = idTransaccion;
        this.referenciaExterna = referenciaExterna;
        this.monto = monto;
        this.saldoResultante = saldoResultante;
        this.estado = estado;
        this.fecha = fecha;
        this.mensaje = mensaje;
    }

    public Long getIdTransaccion() { return idTransaccion; }
    public String getReferenciaExterna() { return referenciaExterna; }
    public BigDecimal getMonto() { return monto; }
    public BigDecimal getSaldoResultante() { return saldoResultante; }
    public String getEstado() { return estado; }
    public LocalDateTime getFecha() { return fecha; }
    public String getMensaje() { return mensaje; }
}
