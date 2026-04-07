package fe.banco_digital.exception;

public class CuentaYaCerradaException extends RuntimeException {
    public CuentaYaCerradaException(String numeroCuenta) {
        super("La cuenta " + numeroCuenta + " ya se encuentra cerrada.");
    }
}