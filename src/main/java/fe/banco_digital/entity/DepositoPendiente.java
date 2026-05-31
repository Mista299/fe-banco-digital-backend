package fe.banco_digital.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deposito_pendiente")
public class DepositoPendiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String referenciaGateway;

    // Relación a la cuenta destino. Se enlaza por numero_cuenta (columna UNIQUE),
    // no por id, porque el flujo de la pasarela identifica la cuenta por su número.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "numero_cuenta", referencedColumnName = "numero_cuenta", nullable = false)
    private Cuenta cuenta;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDepositoPendiente estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaExpiracion;

    public Long getId() { return id; }

    public String getReferenciaGateway() { return referenciaGateway; }
    public void setReferenciaGateway(String referenciaGateway) { this.referenciaGateway = referenciaGateway; }

    public Cuenta getCuenta() { return cuenta; }
    public void setCuenta(Cuenta cuenta) { this.cuenta = cuenta; }

    // Conveniencia de solo lectura: número de la cuenta destino.
    public String getNumeroCuenta() { return cuenta != null ? cuenta.getNumeroCuenta() : null; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public EstadoDepositoPendiente getEstado() { return estado; }
    public void setEstado(EstadoDepositoPendiente estado) { this.estado = estado; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public void setFechaExpiracion(LocalDateTime fechaExpiracion) { this.fechaExpiracion = fechaExpiracion; }
}
