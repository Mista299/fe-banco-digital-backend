package fe.banco_digital.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CuentaTest {

    @Test
    void gettersYSetters_debenFuncionar() {
        Cuenta cuenta = new Cuenta();

        cuenta.setIdCuenta(1L);
        cuenta.setNumeroCuenta("123");
        cuenta.setSaldo(BigDecimal.valueOf(1000));
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setTipo(TipoCuenta.AHORROS);

        assertEquals(1L, cuenta.getIdCuenta());
        assertEquals("123", cuenta.getNumeroCuenta());
        assertEquals(BigDecimal.valueOf(1000), cuenta.getSaldo());
        assertEquals(EstadoCuenta.ACTIVA, cuenta.getEstado());
        assertEquals(TipoCuenta.AHORROS, cuenta.getTipo());
    }
}
