package fe.banco_digital.exception;

public class CuentaNoEncontradaException extends RuntimeException {
    public CuentaNoEncontradaException(Long idCuenta) {
        super("Cuenta con id " + idCuenta + " no encontrada.");
    }

    public CuentaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}