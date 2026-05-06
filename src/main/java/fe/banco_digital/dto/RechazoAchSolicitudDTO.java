package fe.banco_digital.dto;

import jakarta.validation.constraints.NotBlank;

public class RechazoAchSolicitudDTO {

    @NotBlank(message = "El motivo de rechazo es obligatorio.")
    private String motivo;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
