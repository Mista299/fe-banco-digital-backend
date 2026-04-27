package fe.banco_digital.dto;

import java.util.List;

public class DashboardResponseDTO {
    private String mensajeBienvenida;
    private List<CuentaResumenDTO> cuentas;

    public DashboardResponseDTO(String mensajeBienvenida, List<CuentaResumenDTO> cuentas) {
        this.mensajeBienvenida = mensajeBienvenida;
        this.cuentas = cuentas;
    }

    public String getMensajeBienvenida() { return mensajeBienvenida; }
    public List<CuentaResumenDTO> getCuentas() { return cuentas; }
}