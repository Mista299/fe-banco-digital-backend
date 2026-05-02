package fe.banco_digital.mapper;

import fe.banco_digital.dto.CuentaResumenDTO;
import fe.banco_digital.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CuentaMapperTest {

    private final CuentaMapper mapper = new CuentaMapper();

    @Test
    void aCuentaResumenDTO() {
        Cuenta cuenta = new Cuenta();
        cuenta.setIdCuenta(1L);
        cuenta.setNumeroCuenta("123");
        cuenta.setTipo(TipoCuenta.AHORROS);
        cuenta.setSaldo(BigDecimal.TEN);
        cuenta.setEstado(EstadoCuenta.ACTIVA);

        CuentaResumenDTO dto = mapper.aCuentaResumenDTO(cuenta);

        assertEquals("123", dto.getNumeroCuenta());
        assertEquals("AHORROS", dto.getTipo());
    }
}
