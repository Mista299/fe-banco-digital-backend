package fe.banco_digital.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransferenciaInternacionalResponseDTO {

    private Long idTransaccion;
    private String referenciaSwift;
    private BigDecimal montoUsd;
    private BigDecimal montoCop;
    private BigDecimal tasaCambio;
    private BigDecimal saldoResultante;
    private String estado;
    private LocalDateTime fecha;
    private String mensaje;

    public TransferenciaInternacionalResponseDTO(Long idTransaccion, String referenciaSwift,
                                                  BigDecimal montoUsd, BigDecimal montoCop,
                                                  BigDecimal tasaCambio, BigDecimal saldoResultante,
                                                  String estado, LocalDateTime fecha, String mensaje) {
        this.idTransaccion = idTransaccion;
        this.referenciaSwift = referenciaSwift;
        this.montoUsd = montoUsd;
        this.montoCop = montoCop;
        this.tasaCambio = tasaCambio;
        this.saldoResultante = saldoResultante;
        this.estado = estado;
        this.fecha = fecha;
        this.mensaje = mensaje;
    }

    public Long getIdTransaccion() { return idTransaccion; }
    public String getReferenciaSwift() { return referenciaSwift; }
    public BigDecimal getMontoUsd() { return montoUsd; }
    public BigDecimal getMontoCop() { return montoCop; }
    public BigDecimal getTasaCambio() { return tasaCambio; }
    public BigDecimal getSaldoResultante() { return saldoResultante; }
    public String getEstado() { return estado; }
    public LocalDateTime getFecha() { return fecha; }
    public String getMensaje() { return mensaje; }
}
