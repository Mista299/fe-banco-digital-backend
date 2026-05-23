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
@Table(name = "transferencia_externa")
public class TransferenciaExterna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transf_ext")
    private Long idTransfExt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cuenta_origen", nullable = false)
    private Cuenta cuentaOrigen;

    @Column(name = "banco_destino", nullable = false, length = 120)
    private String bancoDestino;

    @Column(name = "tipo_cuenta_destino", nullable = false, length = 30)
    private String tipoCuentaDestino;

    @Column(name = "numero_cuenta_destino", nullable = false, length = 40)
    private String numeroCuentaDestino;

    @Column(name = "tipo_documento_receptor", nullable = false, length = 30)
    private String tipoDocumentoReceptor;

    @Column(name = "numero_documento_receptor", nullable = false, length = 40)
    private String numeroDocumentoReceptor;

    @Column(name = "nombre_receptor", nullable = false, length = 150)
    private String nombreReceptor;

    @Column(name = "monto", nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoTransferenciaExterna estado;

    @Column(name = "referencia_externa", length = 80)
    private String referenciaExterna;

    @Column(name = "motivo_rechazo", length = 255)
    private String motivoRechazo;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_transf_original")
    private TransferenciaExterna transferenciaOriginal;

    public Long getIdTransfExt() { return idTransfExt; }
    public void setIdTransfExt(Long idTransfExt) { this.idTransfExt = idTransfExt; }

    public Cuenta getCuentaOrigen() { return cuentaOrigen; }
    public void setCuentaOrigen(Cuenta cuentaOrigen) { this.cuentaOrigen = cuentaOrigen; }

    public String getBancoDestino() { return bancoDestino; }
    public void setBancoDestino(String bancoDestino) { this.bancoDestino = bancoDestino; }

    public String getTipoCuentaDestino() { return tipoCuentaDestino; }
    public void setTipoCuentaDestino(String tipoCuentaDestino) { this.tipoCuentaDestino = tipoCuentaDestino; }

    public String getNumeroCuentaDestino() { return numeroCuentaDestino; }
    public void setNumeroCuentaDestino(String numeroCuentaDestino) { this.numeroCuentaDestino = numeroCuentaDestino; }

    public String getTipoDocumentoReceptor() { return tipoDocumentoReceptor; }
    public void setTipoDocumentoReceptor(String tipoDocumentoReceptor) { this.tipoDocumentoReceptor = tipoDocumentoReceptor; }

    public String getNumeroDocumentoReceptor() { return numeroDocumentoReceptor; }
    public void setNumeroDocumentoReceptor(String numeroDocumentoReceptor) { this.numeroDocumentoReceptor = numeroDocumentoReceptor; }

    public String getNombreReceptor() { return nombreReceptor; }
    public void setNombreReceptor(String nombreReceptor) { this.nombreReceptor = nombreReceptor; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public EstadoTransferenciaExterna getEstado() { return estado; }
    public void setEstado(EstadoTransferenciaExterna estado) { this.estado = estado; }

    public String getReferenciaExterna() { return referenciaExterna; }
    public void setReferenciaExterna(String referenciaExterna) { this.referenciaExterna = referenciaExterna; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public TransferenciaExterna getTransferenciaOriginal() { return transferenciaOriginal; }
    public void setTransferenciaOriginal(TransferenciaExterna transferenciaOriginal) { this.transferenciaOriginal = transferenciaOriginal; }
}
