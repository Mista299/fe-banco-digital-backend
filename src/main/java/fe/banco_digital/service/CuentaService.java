package fe.banco_digital.service;

import fe.banco_digital.dto.CierreCuentaRespuestaDTO;
import fe.banco_digital.dto.CierreCuentaSolicitudDTO;
import fe.banco_digital.dto.CuentaResumenDTO;

import java.util.List;

public interface CuentaService {

    // Escenarios 1, 2 y 4
    CierreCuentaRespuestaDTO cerrarCuenta(CierreCuentaSolicitudDTO solicitud);

    // Escenario 3
    List<CuentaResumenDTO> obtenerCuentasDelCliente(String username);
}