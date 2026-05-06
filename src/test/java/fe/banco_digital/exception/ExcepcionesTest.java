package fe.banco_digital.exception;

import fe.banco_digital.dto.RechazoDepositoDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ExcepcionesTest {

    @Test
    void accesoNoAutorizado_sinMensaje_usaMensajePorDefecto() {
        AccesoNoAutorizadoException ex = new AccesoNoAutorizadoException();
        assertTrue(ex.getMessage().contains("permiso"));
    }

    @Test
    void accesoNoAutorizado_conMensaje_usaMensajePersonalizado() {
        AccesoNoAutorizadoException ex = new AccesoNoAutorizadoException("Operación denegada");
        assertEquals("Operación denegada", ex.getMessage());
    }

    @Test
    void autenticacionFallida_mensajeContieneBloqueado() {
        AutenticacionFallidaException ex = new AutenticacionFallidaException();
        assertTrue(ex.getMessage().contains("bloqueado"));
    }

    @Test
    void camposObligatorios_mensajeCorrecto() {
        CamposObligatoriosException ex = new CamposObligatoriosException();
        assertTrue(ex.getMessage().contains("obligatorios"));
    }

    @Test
    void clienteNoEncontrado_incluyeId() {
        ClienteNoEncontradoException ex = new ClienteNoEncontradoException(42L);
        assertTrue(ex.getMessage().contains("42"));
    }

    @Test
    void clienteYaTieneUsuario_incluyeIdCliente() {
        ClienteYaTieneUsuarioException ex = new ClienteYaTieneUsuarioException(7L);
        assertTrue(ex.getMessage().contains("7"));
    }

    @Test
    void credencialesInvalidas_mensajeCorrecto() {
        CredencialesInvalidasException ex = new CredencialesInvalidasException();
        assertNotNull(ex.getMessage());
    }

    @Test
    void cuentaBloqueada_incluyeNumeroCuenta() {
        CuentaBloqueadaException ex = new CuentaBloqueadaException("00010001");
        assertTrue(ex.getMessage().contains("00010001"));
    }

    @Test
    void cuentaNoEncontrada_porId_incluyeId() {
        CuentaNoEncontradaException ex = new CuentaNoEncontradaException(99L);
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void cuentaNoEncontrada_porMensaje_usaMensaje() {
        CuentaNoEncontradaException ex = new CuentaNoEncontradaException("Cuenta interna no hallada");
        assertEquals("Cuenta interna no hallada", ex.getMessage());
    }

    @Test
    void cuentaYaCerrada_incluyeNumeroCuenta() {
        CuentaYaCerradaException ex = new CuentaYaCerradaException("00020002");
        assertTrue(ex.getMessage().contains("00020002"));
    }

    @Test
    void depositoRechazado_exponeRechazo() {
        RechazoDepositoDTO rechazo = new RechazoDepositoDTO(
                "Cuenta bloqueada", "00010001", BigDecimal.TEN, "REF-XYZ", "ATM");
        DepositoRechazadoException ex = new DepositoRechazadoException(rechazo);
        assertSame(rechazo, ex.getRechazo());
        assertEquals("Cuenta bloqueada", ex.getMessage());
    }

    @Test
    void emailYaExiste_incluyeEmail() {
        EmailYaExisteException ex = new EmailYaExisteException("correo@test.com");
        assertTrue(ex.getMessage().contains("correo@test.com"));
    }

    @Test
    void identificacionDuplicada_incluyeDocumento() {
        IdentificacionDuplicadaException ex = new IdentificacionDuplicadaException("12345678");
        assertTrue(ex.getMessage().contains("12345678"));
    }

    @Test
    void retiroRechazado_mensajeCorrecto() {
        RetiroRechazadoException ex = new RetiroRechazadoException("Monto inválido");
        assertEquals("Monto inválido", ex.getMessage());
    }

    @Test
    void saldoInsuficiente_mensajeCorrecto() {
        SaldoInsuficienteException ex = new SaldoInsuficienteException();
        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
    }

    @Test
    void saldoPendiente_mensajeCorrecto() {
        SaldoPendienteException ex = new SaldoPendienteException();
        assertTrue(ex.getMessage().contains("saldo"));
    }

    @Test
    void tokenExpirado_mensajeCorrecto() {
        TokenExpiradoException ex = new TokenExpiradoException();
        assertTrue(ex.getMessage().contains("expiró"));
    }

    @Test
    void tokenInvalido_mensajeCorrecto() {
        TokenInvalidoException ex = new TokenInvalidoException();
        assertTrue(ex.getMessage().contains("válido"));
    }

    @Test
    void usuarioYaExiste_incluyeUsername() {
        UsuarioYaExisteException ex = new UsuarioYaExisteException("admin");
        assertTrue(ex.getMessage().contains("admin"));
    }
}
