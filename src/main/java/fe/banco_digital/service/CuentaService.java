package fe.banco_digital.service;

import fe.banco_digital.dto.CierreCuentaRespuestaDTO;
import fe.banco_digital.dto.CierreCuentaSolicitudDTO;
import fe.banco_digital.dto.DashboardResponseDTO;


public interface CuentaService {

    // Escenarios 1, 2 y 4
    CierreCuentaRespuestaDTO cerrarCuenta(CierreCuentaSolicitudDTO solicitud, String username);

    // Escenario 3
    DashboardResponseDTO obtenerCuentasDelCliente(String username);
}