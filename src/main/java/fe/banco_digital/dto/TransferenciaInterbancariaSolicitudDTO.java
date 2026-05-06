package fe.banco_digital.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransferenciaInterbancariaSolicitudDTO {

    @NotNull(message = "El id de la cuenta origen es obligatorio.")
    private Long idCuentaOrigen;

    @NotBlank(message = "El banco destino es obligatorio.")
    private String bancoDestino;

    @NotBlank(message = "El tipo de cuenta destino es obligatorio.")
    private String tipoCuentaDestino;

    @NotBlank(message = "El número de cuenta destino es obligatorio.")
    private String numeroCuentaDestino;

    @NotBlank(message = "El tipo de documento del receptor es obligatorio.")
    private String tipoDocumentoReceptor;

    @NotBlank(message = "El número de documento del receptor es obligatorio.")
    private String numeroDocumentoReceptor;

    @NotBlank(message = "El nombre del receptor es obligatorio.")
    private String nombreReceptor;

    @NotNull(message = "El monto es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero.")
    private BigDecimal monto;

    public Long getIdCuentaOrigen() { return idCuentaOrigen; }
    public void setIdCuentaOrigen(Long idCuentaOrigen) { this.idCuentaOrigen = idCuentaOrigen; }
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
}
