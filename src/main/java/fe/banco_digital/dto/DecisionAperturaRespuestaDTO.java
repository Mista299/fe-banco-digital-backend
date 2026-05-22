package fe.banco_digital.dto;

public class DecisionAperturaRespuestaDTO {

    private Long idCuenta;
    private String numeroCuenta;
    private String estado;
    private String mensaje;

    public DecisionAperturaRespuestaDTO(Long idCuenta, String numeroCuenta, String estado, String mensaje) {
        this.idCuenta = idCuenta;
        this.numeroCuenta = numeroCuenta;
        this.estado = estado;
        this.mensaje = mensaje;
    }

    public Long getIdCuenta() { return idCuenta; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public String getEstado() { return estado; }
    public String getMensaje() { return mensaje; }
}
