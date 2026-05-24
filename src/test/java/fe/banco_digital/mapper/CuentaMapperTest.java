package fe.banco_digital.mapper;

import fe.banco_digital.dto.CuentaResumenDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.TipoCuenta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CuentaMapperTest {

    private final CuentaMapper mapper = new CuentaMapper();

    @Test
    void aCuentaResumenDTO_cuentaActiva_permiteTransacciones() {
        Cuenta cuenta = buildCuenta(EstadoCuenta.ACTIVA, new BigDecimal("500.00"));

        CuentaResumenDTO dto = mapper.aCuentaResumenDTO(cuenta);

        assertTrue(dto.isPermiteTransacciones());
        assertNull(dto.getEtiquetaVisual());
        assertEquals("ACTIVA", dto.getEstado());
        assertEquals("AHORROS", dto.getTipo());
        assertEquals(new BigDecimal("500.00"), dto.getSaldo());
        assertEquals("12345678", dto.getNumeroCuenta());
    }

    @Test
    void aCuentaResumenDTO_cuentaCerrada_bloqueaTransaccionesYMuestraEtiqueta() {
        Cuenta cuenta = buildCuenta(EstadoCuenta.INACTIVA, BigDecimal.ZERO);

        CuentaResumenDTO dto = mapper.aCuentaResumenDTO(cuenta);

        assertFalse(dto.isPermiteTransacciones());
        assertEquals("Cuenta Cerrada", dto.getEtiquetaVisual());
        assertEquals("INACTIVA", dto.getEstado());
    }

    @Test
    void aCuentaResumenDTO_cuentaBloqueada_noPermiteTransaccionesNiEtiqueta() {
        Cuenta cuenta = buildCuenta(EstadoCuenta.BLOQUEADA, new BigDecimal("200.00"));

        CuentaResumenDTO dto = mapper.aCuentaResumenDTO(cuenta);

        assertFalse(dto.isPermiteTransacciones());
        assertNull(dto.getEtiquetaVisual());
    }

    private Cuenta buildCuenta(EstadoCuenta estado, BigDecimal saldo) {
        Cuenta c = new Cuenta();
        c.setIdCuenta(1L);
        c.setNumeroCuenta("12345678");
        c.setTipo(TipoCuenta.AHORROS);
        c.setSaldo(saldo);
        c.setEstado(estado);
        return c;
    }
}
