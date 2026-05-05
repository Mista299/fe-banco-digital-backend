package fe.banco_digital.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TransferenciaSolicitudDTO {

    @NotNull(message = "El id de la cuenta origen es obligatorio.")
    private Long idCuentaOrigen;

    @NotBlank(message = "El número de cuenta destino es obligatorio.")
    private String numeroCuentaDestino;

    @NotNull(message = "El monto es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero.")
    private BigDecimal monto;

    public Long getIdCuentaOrigen() { return idCuentaOrigen; }
    public void setIdCuentaOrigen(Long idCuentaOrigen) { this.idCuentaOrigen = idCuentaOrigen; }

    public String getNumeroCuentaDestino() { return numeroCuentaDestino; }
    public void setNumeroCuentaDestino(String numeroCuentaDestino) { this.numeroCuentaDestino = numeroCuentaDestino; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
}
