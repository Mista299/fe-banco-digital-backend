package fe.banco_digital.dto;

import jakarta.validation.constraints.NotBlank;

public class RechazoSwiftSolicitudDTO {

    @NotBlank(message = "El motivo del rechazo es obligatorio.")
    private String motivo;

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
