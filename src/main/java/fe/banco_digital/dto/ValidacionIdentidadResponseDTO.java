package fe.banco_digital.dto;

public class ValidacionIdentidadResponseDTO {

    private boolean disponible;
    private String mensaje;

    public ValidacionIdentidadResponseDTO() {
    }

    public ValidacionIdentidadResponseDTO(boolean disponible, String mensaje) {
        this.disponible = disponible;
        this.mensaje = mensaje;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
