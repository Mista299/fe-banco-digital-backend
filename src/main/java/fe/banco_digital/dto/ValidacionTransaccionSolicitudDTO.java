package fe.banco_digital.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ValidacionTransaccionSolicitudDTO {

    @NotNull(message = "El id de la cuenta origen es obligatorio.")
    private Long idCuentaOrigen;

    @NotNull(message = "El monto es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero.")
    private BigDecimal monto;

    @NotBlank(message = "El tipo de operación es obligatorio.")
    private String tipoOperacion;

    public Long getIdCuentaOrigen() { return idCuentaOrigen; }
    public void setIdCuentaOrigen(Long idCuentaOrigen) { this.idCuentaOrigen = idCuentaOrigen; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getTipoOperacion() { return tipoOperacion; }
    public void setTipoOperacion(String tipoOperacion) { this.tipoOperacion = tipoOperacion; }
}
