package fe.banco_digital.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transferencia")
public class Transferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transferencia")
    private Long idTransferencia;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cuenta_origen", nullable = false)
    private Cuenta cuentaOrigen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cuenta_destino", nullable = false)
    private Cuenta cuentaDestino;

    @Column(name = "monto", nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoTransferencia estado;

    public Long getIdTransferencia() { return idTransferencia; }
    public void setIdTransferencia(Long idTransferencia) { this.idTransferencia = idTransferencia; }

    public Cuenta getCuentaOrigen() { return cuentaOrigen; }
    public void setCuentaOrigen(Cuenta cuentaOrigen) { this.cuentaOrigen = cuentaOrigen; }

    public Cuenta getCuentaDestino() { return cuentaDestino; }
    public void setCuentaDestino(Cuenta cuentaDestino) { this.cuentaDestino = cuentaDestino; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public EstadoTransferencia getEstado() { return estado; }
    public void setEstado(EstadoTransferencia estado) { this.estado = estado; }
}
