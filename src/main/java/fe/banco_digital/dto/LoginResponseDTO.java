package fe.banco_digital.dto;

public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String tipo = "Bearer";
    private long expiraEn;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public long getExpiraEn() { return expiraEn; }
    public void setExpiraEn(long expiraEn) { this.expiraEn = expiraEn; }
}
