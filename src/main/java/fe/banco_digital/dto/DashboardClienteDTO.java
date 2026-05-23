package fe.banco_digital.dto;

import java.util.List;

public class DashboardClienteDTO {

    private String nombre;
    private String email;
    private List<CuentaResumenDTO> cuentas;

    public DashboardClienteDTO(String nombre, String email, List<CuentaResumenDTO> cuentas) {
        this.nombre = nombre;
        this.email = email;
        this.cuentas = cuentas;
    }

    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public List<CuentaResumenDTO> getCuentas() { return cuentas; }
}
