package fe.banco_digital.dto;

import java.time.LocalDateTime;

public class AuditoriaDTO {

    private Long idAuditoria;
    private String accion;
    private String usuarioUsername;
    private String usuarioRol;
    private LocalDateTime fecha;
    private String detalle;

    public AuditoriaDTO(Long idAuditoria, String accion, String usuarioUsername,
                        String usuarioRol, LocalDateTime fecha, String detalle) {
        this.idAuditoria = idAuditoria;
        this.accion = accion;
        this.usuarioUsername = usuarioUsername;
        this.usuarioRol = usuarioRol;
        this.fecha = fecha;
        this.detalle = detalle;
    }

    public Long getIdAuditoria() { return idAuditoria; }
    public String getAccion() { return accion; }
    public String getUsuarioUsername() { return usuarioUsername; }
    public String getUsuarioRol() { return usuarioRol; }
    public LocalDateTime getFecha() { return fecha; }
    public String getDetalle() { return detalle; }
}
