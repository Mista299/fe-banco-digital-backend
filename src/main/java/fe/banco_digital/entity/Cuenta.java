package fe.banco_digital.entity;
java.math.BigDecimal;

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
import jakarta.persistence.Version;

@Entity
@Table(name = "cuenta")
public class Cuenta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_cuenta")
	private Long idCuenta;

	@Version
	@Column(name = "version", nullable = false)
	private Long version = 0L;

	@Column(name = "numero_cuenta", nullable = false, unique = true)
	private String numeroCuenta;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo", nullable = false)
	private TipoCuenta tipo;

	@Column(name = "saldo", precision = 19, scale = 4)
	private BigDecimal saldo = BigDecimal.ZERO;

	// NUEVOS CAMPOS (HU10)
    @Column(name = "saldo_disponible", precision = 19, scale = 4)
    private BigDecimal saldoDisponible = BigDecimal.ZERO;

    @Column(name = "saldo_reservado", precision = 19, scale = 4)
    private BigDecimal saldoReservado = BigDecimal.ZERO;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false)
	private EstadoCuenta estado = EstadoCuenta.ACTIVA;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_cliente", nullable = false)
	private Cliente cliente;

	public Long getIdCuenta() {
		return idCuenta;
	}

	public void setIdCuenta(Long idCuenta) {
		this.idCuenta = idCuenta;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public String getNumeroCuenta() {
		return numeroCuenta;
	}

	public void setNumeroCuenta(String numeroCuenta) {
		this.numeroCuenta = numeroCuenta;
	}

	public TipoCuenta getTipo() {
		return tipo;
	}

	public void setTipo(TipoCuenta tipo) {
		this.tipo = tipo;
	}

	public BigDecimal getSaldo() {
		return saldo;
	}
	public BigDecimal getSaldoDisponible() {
    return saldoDisponible;
   }

   public void setSaldoDisponible(BigDecimal saldoDisponible) {
    this.saldoDisponible = saldoDisponible;
    }

    public BigDecimal getSaldoReservado() {
    return saldoReservado;
   }

public void setSaldoReservado(BigDecimal saldoReservado) {
    this.saldoReservado = saldoReservado;
   }

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

	public EstadoCuenta getEstado() {
		return estado;
	}

	public void setEstado(EstadoCuenta estado) {
		this.estado = estado;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}


}
