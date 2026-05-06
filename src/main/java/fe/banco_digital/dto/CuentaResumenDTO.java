package fe.banco_digital.dto;

import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;

import java.math.BigDecimal;

public class CuentaResumenDTO {

    private Long idCuenta;
    private String numeroCuenta;
    private String tipo;
    private BigDecimal saldo;
    private String estado;
    private boolean permiteTransacciones;  // Escenario 3
    private String etiquetaVisual;         // "Cuenta Cerrada" — Escenario 3

    // Constructor desde entidad — patrón del equipo
    public CuentaResumenDTO(Cuenta cuenta) {
        boolean estaCerrada = cuenta.getEstado() == EstadoCuenta.INACTIVA;
        this.idCuenta = cuenta.getIdCuenta();
        this.numeroCuenta = cuenta.getNumeroCuenta();
        this.tipo = cuenta.getTipo().name();
        this.saldo = cuenta.getSaldo();
        this.estado = cuenta.getEstado().name();
        this.permiteTransacciones = cuenta.getEstado() == EstadoCuenta.ACTIVA;
        this.etiquetaVisual = estaCerrada ? "Cuenta Cerrada" : null;
    }

    public Long getIdCuenta() { return idCuenta; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public String getTipo() { return tipo; }
    public BigDecimal getSaldo() { return saldo; }
    public String getEstado() { return estado; }
    public boolean isPermiteTransacciones() { return permiteTransacciones; }
    public String getEtiquetaVisual() { return etiquetaVisual; }
}
