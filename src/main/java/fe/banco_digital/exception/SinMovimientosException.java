package fe.banco_digital.exception;

public class SinMovimientosException extends RuntimeException {

    public SinMovimientosException(String mensaje) {
        super(mensaje);
    }
}
