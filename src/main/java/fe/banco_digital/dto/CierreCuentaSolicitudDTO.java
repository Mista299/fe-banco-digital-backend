package fe.banco_digital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CierreCuentaSolicitudDTO {

    @NotNull(message = "El id de la cuenta es obligatorio.")
    private Long idCuenta;

    @NotBlank(message = "La contraseña es obligatoria.")
    private String contrasena;

    public CierreCuentaSolicitudDTO() {}

    public Long getIdCuenta() { return idCuenta; }
    public void setIdCuenta(Long idCuenta) { this.idCuenta = idCuenta; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
