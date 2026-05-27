package fe.banco_digital.dto;

import java.math.BigDecimal;
import java.util.List;

public class ExtractoDatosDTO {

    private String numeroCuentaEnmascarado;
    private String tipoCuenta;
    private String nombreTitular;
    private String documento;
    private int anio;
    private int mes;
    private BigDecimal saldoInicial;
    private BigDecimal saldoFinal;
    private BigDecimal totalCreditos;
    private BigDecimal totalDebitos;
    private List<MovimientoDTO> movimientos;

    public String getNumeroCuentaEnmascarado() { return numeroCuentaEnmascarado; }
    public void setNumeroCuentaEnmascarado(String numeroCuentaEnmascarado) { this.numeroCuentaEnmascarado = numeroCuentaEnmascarado; }

    public String getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta; }

    public String getNombreTitular() { return nombreTitular; }
    public void setNombreTitular(String nombreTitular) { this.nombreTitular = nombreTitular; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }

    public BigDecimal getSaldoInicial() { return saldoInicial; }
    public void setSaldoInicial(BigDecimal saldoInicial) { this.saldoInicial = saldoInicial; }

    public BigDecimal getSaldoFinal() { return saldoFinal; }
    public void setSaldoFinal(BigDecimal saldoFinal) { this.saldoFinal = saldoFinal; }

    public BigDecimal getTotalCreditos() { return totalCreditos; }
    public void setTotalCreditos(BigDecimal totalCreditos) { this.totalCreditos = totalCreditos; }

    public BigDecimal getTotalDebitos() { return totalDebitos; }
    public void setTotalDebitos(BigDecimal totalDebitos) { this.totalDebitos = totalDebitos; }

    public List<MovimientoDTO> getMovimientos() { return movimientos; }
    public void setMovimientos(List<MovimientoDTO> movimientos) { this.movimientos = movimientos; }
}
