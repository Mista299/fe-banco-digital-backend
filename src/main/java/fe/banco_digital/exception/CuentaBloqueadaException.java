package fe.banco_digital.exception;

public class CuentaBloqueadaException extends RuntimeException {
    public CuentaBloqueadaException(String numeroCuenta) {
        super("La cuenta " + numeroCuenta + " se encuentra bloqueada y no puede operar.");
    }
}
