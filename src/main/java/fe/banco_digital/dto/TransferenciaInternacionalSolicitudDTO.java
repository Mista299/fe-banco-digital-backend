package fe.banco_digital.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class TransferenciaInternacionalSolicitudDTO {

    @NotNull(message = "El id de la cuenta origen es obligatorio.")
    private Long idCuentaOrigen;

    @NotBlank(message = "El banco destino es obligatorio.")
    private String bancoDestino;

    @NotBlank(message = "El código SWIFT es obligatorio.")
    private String codigoSwift;

    @NotBlank(message = "El país destino es obligatorio.")
    private String paisDestino;

    @NotBlank(message = "El tipo de cuenta destino es obligatorio.")
    private String tipoCuentaDestino;

    @NotBlank(message = "El IBAN o número de cuenta destino es obligatorio.")
    private String ibanCuentaDestino;

    @NotBlank(message = "El tipo de documento del receptor es obligatorio.")
    private String tipoDocumentoReceptor;

    @NotBlank(message = "El número de documento del receptor es obligatorio.")
    private String numeroDocumentoReceptor;

    @NotBlank(message = "El nombre del receptor es obligatorio.")
    private String nombreReceptor;

    @NotNull(message = "El monto en USD es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero.")
    private BigDecimal montoUsd;

    @NotNull(message = "La tasa de cambio es obligatoria.")
    @DecimalMin(value = "0.000001", message = "La tasa de cambio debe ser positiva.")
    private BigDecimal tasaCambio;

    private String moneda = "USD";

    public Long getIdCuentaOrigen() { return idCuentaOrigen; }
    public void setIdCuentaOrigen(Long idCuentaOrigen) { this.idCuentaOrigen = idCuentaOrigen; }

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

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
}
