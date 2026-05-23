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
@Table(name = "transferencia_internacional")
public class TransferenciaInternacional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transf_int")
    private Long idTransfInt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cuenta_origen", nullable = false)
    private Cuenta cuentaOrigen;

    @Column(name = "banco_destino", nullable = false, length = 150)
    private String bancoDestino;

    @Column(name = "codigo_swift", nullable = false, length = 11)
    private String codigoSwift;

    @Column(name = "pais_destino", nullable = false, length = 60)
    private String paisDestino;

    @Column(name = "tipo_cuenta_destino", nullable = false, length = 30)
    private String tipoCuentaDestino;

    @Column(name = "iban_cuenta_destino", nullable = false, length = 34)
    private String ibanCuentaDestino;

    @Column(name = "tipo_documento_receptor", nullable = false, length = 30)
    private String tipoDocumentoReceptor;

    @Column(name = "numero_documento_receptor", nullable = false, length = 40)
    private String numeroDocumentoReceptor;

    @Column(name = "nombre_receptor", nullable = false, length = 150)
    private String nombreReceptor;

    @Column(name = "monto_usd", nullable = false, precision = 19, scale = 4)
    private BigDecimal montoUsd;

    @Column(name = "tasa_cambio", nullable = false, precision = 19, scale = 6)
    private BigDecimal tasaCambio;

    @Column(name = "monto_cop", nullable = false, precision = 19, scale = 4)
    private BigDecimal montoCop;

    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda = "USD";

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoTransferenciaInternacional estado;

    @Column(name = "referencia_swift", length = 80)
    private String referenciaSwift;

    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;

    public Long getIdTransfInt() { return idTransfInt; }
    public void setIdTransfInt(Long idTransfInt) { this.idTransfInt = idTransfInt; }

    public Cuenta getCuentaOrigen() { return cuentaOrigen; }
    public void setCuentaOrigen(Cuenta cuentaOrigen) { this.cuentaOrigen = cuentaOrigen; }

    public String getBancoDestino() { return bancoDestino; }
    public void setBancoDestino(String bancoDestino) { this.bancoDestino = bancoDestino; }

    public String getCodigoSwift() { return codigoSwift; }
    public void setCodigoSwift(String codigoSwift) { this.codigoSwift = codigoSwift; }

    public String getPaisDestino() { return paisDestino; }
    public void setPaisDestino(String paisDestino) { this.paisDestino = paisDestino; }

    public String getTipoCuentaDestino() { return tipoCuentaDestino; }
    public void setTipoCuentaDestino(String tipoCuentaDestino) { this.tipoCuentaDestino = tipoCuentaDestino; }

    public String getIbanCuentaDestino() { return ibanCuentaDestino; }
    public void setIbanCuentaDestino(String ibanCuentaDestino) { this.ibanCuentaDestino = ibanCuentaDestino; }

    public String getTipoDocumentoReceptor() { return tipoDocumentoReceptor; }
    public void setTipoDocumentoReceptor(String tipoDocumentoReceptor) { this.tipoDocumentoReceptor = tipoDocumentoReceptor; }

    public String getNumeroDocumentoReceptor() { return numeroDocumentoReceptor; }
    public void setNumeroDocumentoReceptor(String numeroDocumentoReceptor) { this.numeroDocumentoReceptor = numeroDocumentoReceptor; }

    public String getNombreReceptor() { return nombreReceptor; }
    public void setNombreReceptor(String nombreReceptor) { this.nombreReceptor = nombreReceptor; }

    public BigDecimal getMontoUsd() { return montoUsd; }
    public void setMontoUsd(BigDecimal montoUsd) { this.montoUsd = montoUsd; }

    public BigDecimal getTasaCambio() { return tasaCambio; }
    public void setTasaCambio(BigDecimal tasaCambio) { this.tasaCambio = tasaCambio; }

    public BigDecimal getMontoCop() { return montoCop; }
    public void setMontoCop(BigDecimal montoCop) { this.montoCop = montoCop; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public EstadoTransferenciaInternacional getEstado() { return estado; }
    public void setEstado(EstadoTransferenciaInternacional estado) { this.estado = estado; }

    public String getReferenciaSwift() { return referenciaSwift; }
    public void setReferenciaSwift(String referenciaSwift) { this.referenciaSwift = referenciaSwift; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }
}
