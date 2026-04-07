package fe.banco_digital.dto;

public class CierreCuentaSolicitudDTO {
    private Long idCuenta;
    private String username;
    private String contrasena;  // "password" → español

    public CierreCuentaSolicitudDTO() {}

    public Long getIdCuenta() { return idCuenta; }
    public String getUsername() { return username; }
    public String getContrasena() { return contrasena; }

    public void setIdCuenta(Long idCuenta) { this.idCuenta = idCuenta; }
    public void setUsername(String username) { this.username = username; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
