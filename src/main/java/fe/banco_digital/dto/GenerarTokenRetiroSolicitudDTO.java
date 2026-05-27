package fe.banco_digital.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class GenerarTokenRetiroSolicitudDTO {

    @NotNull(message = "El idCuenta es obligatorio")
    private Long idCuenta;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    public Long getIdCuenta() { return idCuenta; }
    public BigDecimal getMonto() { return monto; }
}
