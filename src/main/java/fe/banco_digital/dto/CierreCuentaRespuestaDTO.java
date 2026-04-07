package fe.banco_digital.dto;

public class CierreCuentaRespuestaDTO {

    private String numeroCuenta;
    private String estado;
    private String mensaje;

    // Constructor que recibe los datos directamente — patrón del equipo
    public CierreCuentaRespuestaDTO(String numeroCuenta, String estado, String mensaje) {
        this.numeroCuenta = numeroCuenta;
        this.estado = estado;
        this.mensaje = mensaje;
    }

    public String getNumeroCuenta() { return numeroCuenta; }
    public String getEstado() { return estado; }
    public String getMensaje() { return mensaje; }
}
