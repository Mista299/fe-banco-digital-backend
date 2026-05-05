package fe.banco_digital.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class NotificacionRetiroDTO {

    @NotBlank(message = "El codigoToken es obligatorio")
    private String codigoToken;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    public String getCodigoToken() { return codigoToken; }
    public BigDecimal getMonto() { return monto; }
}
