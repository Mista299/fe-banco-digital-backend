package fe.banco_digital.dto;

public class UsuarioRegistradoDTO {

    private Long idUsuario;
    private String username;
    private String estado;

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
