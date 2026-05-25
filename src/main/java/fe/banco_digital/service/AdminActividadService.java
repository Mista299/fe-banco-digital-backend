package fe.banco_digital.service;

import fe.banco_digital.dto.ActividadClienteResponseDTO;
import java.time.LocalDateTime;

public interface AdminActividadService {

    ActividadClienteResponseDTO consultarActividadPorDocumento(
            String documento,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            String tipoMovimiento,      // "RETIRO", "DEPOSITO", "TRANSFERENCIA", etc. — null = todos
            String usernameAdmin);

    ActividadClienteResponseDTO consultarActividadPorNumeroCuenta(
            String numeroCuenta,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            String tipoMovimiento,
            String usernameAdmin);
}