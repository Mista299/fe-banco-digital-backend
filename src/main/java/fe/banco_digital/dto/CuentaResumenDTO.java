package fe.banco_digital.dto;

import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;

import java.math.BigDecimal;

public class CuentaResumenDTO {

    private Long idCuenta;
    private String numeroCuenta;
    private String numeroEnmascarado;
    private String tipo;
    private BigDecimal saldo;
    private BigDecimal saldoDisponible;
    private BigDecimal saldoReservado;
    private String estado;
    private boolean permiteTransacciones;
    private String etiquetaVisual;
    private String cvc;
    private String nombreCliente;

    public CuentaResumenDTO(Cuenta cuenta) {
        boolean estaCerrada = cuenta.getEstado() == EstadoCuenta.INACTIVA;
        this.idCuenta          = cuenta.getIdCuenta();
        this.numeroCuenta      = cuenta.getNumeroCuenta();
        this.numeroEnmascarado = enmascarar(cuenta.getNumeroCuenta());
        this.tipo              = cuenta.getTipo().name();
        this.saldo             = cuenta.getSaldo();
        this.saldoDisponible   = cuenta.getSaldoDisponible();
        this.saldoReservado    = cuenta.getSaldoReservado();
        this.estado            = cuenta.getEstado().name();
        this.permiteTransacciones = cuenta.getEstado() == EstadoCuenta.ACTIVA;
        this.etiquetaVisual    = estaCerrada ? "Cuenta Cerrada" : null;
        this.cvc               = cuenta.getCvc() != null ? cuenta.getCvc() : derivarCvc(cuenta.getNumeroCuenta());
        this.nombreCliente     = cuenta.getCliente() != null ? cuenta.getCliente().getNombre() : null;
    }

    private static String enmascarar(String numero) {
        if (numero == null || numero.length() <= 4) return numero;
        return "*".repeat(numero.length() - 4) + numero.substring(numero.length() - 4);
    }

    private static String derivarCvc(String numero) {
        if (numero == null) return "000";
        return String.format("%03d", Math.abs(numero.hashCode()) % 1000);
    }

    public Long getIdCuenta() { return idCuenta; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public String getNumeroEnmascarado() { return numeroEnmascarado; }
    public String getTipo() { return tipo; }
    public BigDecimal getSaldo() { return saldo; }
    public BigDecimal getSaldoDisponible() { return saldoDisponible; }
    public BigDecimal getSaldoReservado() { return saldoReservado; }
    public String getEstado() { return estado; }
    public boolean isPermiteTransacciones() { return permiteTransacciones; }
    public String getEtiquetaVisual() { return etiquetaVisual; }
    public String getCvc() { return cvc; }
    public String getNombreCliente() { return nombreCliente; }
}
