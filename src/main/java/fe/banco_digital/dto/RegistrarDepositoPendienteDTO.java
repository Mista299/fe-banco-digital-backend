package fe.banco_digital.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class RegistrarDepositoPendienteDTO {

    @NotBlank(message = "La referencia es obligatoria.")
    private String referenciaGateway;

    @NotBlank(message = "El número de cuenta es obligatorio.")
    private String numeroCuenta;

    @NotNull(message = "El monto es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a cero.")
    private BigDecimal monto;

    public RegistrarDepositoPendienteDTO() {}

    public String getReferenciaGateway() { return referenciaGateway; }
    public void setReferenciaGateway(String referenciaGateway) { this.referenciaGateway = referenciaGateway; }

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
}
