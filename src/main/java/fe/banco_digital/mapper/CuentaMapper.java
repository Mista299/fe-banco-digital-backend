package fe.banco_digital.mapper;

import fe.banco_digital.dto.CuentaResumenDTO;
import fe.banco_digital.entity.Cuenta;
import org.springframework.stereotype.Component;

@Component
public class CuentaMapper {

    /**
     * Convierte una entidad Cuenta a CuentaResumenDTO.
     * La lógica del Escenario 3 (etiqueta visual, bloqueo de transacciones)
     * vive dentro del constructor del DTO.
     */
    public CuentaResumenDTO aCuentaResumenDTO(Cuenta cuenta) {
        return new CuentaResumenDTO(cuenta);
    }
}