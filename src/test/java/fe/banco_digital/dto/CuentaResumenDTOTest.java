package fe.banco_digital.dto;

import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.TipoCuenta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CuentaResumenDTOTest {

    @Test
    void constructor_cuentaActiva() {
        // Arrange
        Cuenta cuenta = new Cuenta();
        cuenta.setIdCuenta(1L);
        cuenta.setNumeroCuenta("123");
        cuenta.setTipo(TipoCuenta.AHORROS);
        cuenta.setSaldo(new BigDecimal("1000"));
        cuenta.setEstado(EstadoCuenta.ACTIVA);

        // Act
        CuentaResumenDTO dto = new CuentaResumenDTO(cuenta);

        // Assert
        assertEquals(1L, dto.getIdCuenta());
        assertEquals("123", dto.getNumeroCuenta());
        assertEquals("AHORROS", dto.getTipo());
        assertEquals(new BigDecimal("1000"), dto.getSaldo());
        assertEquals("ACTIVA", dto.getEstado());

        assertTrue(dto.isPermiteTransacciones());
        assertNull(dto.getEtiquetaVisual());
    }

    @Test
    void constructor_cuentaCerrada() {
        // Arrange
        Cuenta cuenta = new Cuenta();
        cuenta.setIdCuenta(2L);
        cuenta.setNumeroCuenta("456");
        cuenta.setTipo(TipoCuenta.CORRIENTE);
        cuenta.setSaldo(BigDecimal.ZERO);
        cuenta.setEstado(EstadoCuenta.INACTIVA);

        // Act
        CuentaResumenDTO dto = new CuentaResumenDTO(cuenta);

        // Assert
        assertFalse(dto.isPermiteTransacciones());
        assertEquals("Cuenta Cerrada", dto.getEtiquetaVisual());
    }

    @Test
    void constructor_tipoCuentaNull_noDebeRomper() {
        // Este test cubre el caso defensivo
        Cuenta cuenta = new Cuenta();
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldo(BigDecimal.ZERO);

        // ⚠️ No seteamos tipo a propósito

        CuentaResumenDTO dto = new CuentaResumenDTO(cuenta);

        assertNull(dto.getTipo());
    }
}