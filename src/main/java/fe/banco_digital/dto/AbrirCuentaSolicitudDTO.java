package fe.banco_digital.dto;

import fe.banco_digital.entity.TipoCuenta;
import jakarta.validation.constraints.NotNull;

public class AbrirCuentaSolicitudDTO {

    @NotNull(message = "El tipo de cuenta es obligatorio (AHORROS o CORRIENTE)")
    private TipoCuenta tipoCuenta;

    public TipoCuenta getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(TipoCuenta tipoCuenta) { this.tipoCuenta = tipoCuenta; }
}
