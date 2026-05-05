package fe.banco_digital.dto;

import java.math.BigDecimal;

public class ValidacionTransaccionResponseDTO {

    private boolean autorizada;
    private String codigo;
    private String mensaje;
    private Long idCuenta;
    private String estadoCuenta;
    private BigDecimal saldoDisponible;

    public ValidacionTransaccionResponseDTO(boolean autorizada, String codigo, String mensaje,
                                            Long idCuenta, String estadoCuenta, BigDecimal saldoDisponible) {
        this.autorizada = autorizada;
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.idCuenta = idCuenta;
        this.estadoCuenta = estadoCuenta;
        this.saldoDisponible = saldoDisponible;
    }

    public boolean isAutorizada() { return autorizada; }
    public String getCodigo() { return codigo; }
    public String getMensaje() { return mensaje; }
    public Long getIdCuenta() { return idCuenta; }
    public String getEstadoCuenta() { return estadoCuenta; }
    public BigDecimal getSaldoDisponible() { return saldoDisponible; }
}
