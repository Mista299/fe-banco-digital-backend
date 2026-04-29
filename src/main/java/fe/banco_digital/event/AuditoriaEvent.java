package fe.banco_digital.event;

import org.springframework.context.ApplicationEvent;

public class AuditoriaEvent extends ApplicationEvent {

    private final String accion;
    private final Long idUsuario;
    private final String detalle;

    public AuditoriaEvent(Object source, String accion, Long idUsuario, String detalle) {
        super(source);
        this.accion = accion;
        this.idUsuario = idUsuario;
        this.detalle = detalle;
    }

    public String getAccion() { return accion; }
    public Long getIdUsuario() { return idUsuario; }
    public String getDetalle() { return detalle; }
}
