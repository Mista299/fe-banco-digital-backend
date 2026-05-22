package fe.banco_digital.dto;

import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.TipoCuenta;

public class SolicitudPendienteDTO {

    private Long idCuenta;
    private String numeroCuenta;
    private TipoCuenta tipo;
    private EstadoCuenta estado;
    private String nombreCliente;
    private Long idCliente;

    public SolicitudPendienteDTO(Cuenta cuenta) {
        this.idCuenta = cuenta.getIdCuenta();
        this.numeroCuenta = cuenta.getNumeroCuenta();
        this.tipo = cuenta.getTipo();
        this.estado = cuenta.getEstado();
        this.nombreCliente = cuenta.getCliente().getNombre();
        this.idCliente = cuenta.getCliente().getIdCliente();
    }

    public Long getIdCuenta() { return idCuenta; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public TipoCuenta getTipo() { return tipo; }
    public EstadoCuenta getEstado() { return estado; }
    public String getNombreCliente() { return nombreCliente; }
    public Long getIdCliente() { return idCliente; }
}
