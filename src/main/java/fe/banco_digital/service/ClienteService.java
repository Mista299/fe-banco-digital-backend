package fe.banco_digital.service;

import fe.banco_digital.dto.ActualizarClienteDTO;
import fe.banco_digital.dto.DashboardClienteDTO;

public interface ClienteService {

    void actualizar(ActualizarClienteDTO dto, String username);

    DashboardClienteDTO obtenerDashboard(String username);
}
