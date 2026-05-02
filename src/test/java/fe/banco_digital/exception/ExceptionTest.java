package fe.banco_digital.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionsTest {

    @Test
    void cuentaNoEncontrada_debeContenerIdEnMensaje() {
        Long id = 1L;

        CuentaNoEncontradaException ex = new CuentaNoEncontradaException(id);

        assertNotNull(ex);
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains("1"));
    }

    @Test
    void cuentaYaCerrada_debeContenerNumeroCuenta() {
        String numeroCuenta = "123456";

        CuentaYaCerradaException ex = new CuentaYaCerradaException(numeroCuenta);

        assertNotNull(ex);
        assertNotNull(ex.getMessage());
        assertTrue(ex.getMessage().contains(numeroCuenta));
    }

    @Test
    void saldoPendiente_debeTenerMensaje() {
        SaldoPendienteException ex = new SaldoPendienteException();

        assertNotNull(ex);
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isEmpty());
    }

    @Test
    void autenticacionFallida_debeTenerMensaje() {
        AutenticacionFallidaException ex = new AutenticacionFallidaException();

        assertNotNull(ex);
        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isEmpty());
    }

    @Test
    void excepciones_debenSerRuntimeException() {
        assertTrue(new CuentaNoEncontradaException(1L) instanceof RuntimeException);
        assertTrue(new CuentaYaCerradaException("123") instanceof RuntimeException);
        assertTrue(new SaldoPendienteException() instanceof RuntimeException);
        assertTrue(new AutenticacionFallidaException() instanceof RuntimeException);
    }
}