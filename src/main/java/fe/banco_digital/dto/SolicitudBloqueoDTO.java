package fe.banco_digital.dto;

import jakarta.validation.constraints.NotBlank;

public class SolicitudBloqueoDTO {

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
