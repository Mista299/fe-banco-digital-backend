package fe.banco_digital.exception;

public class RetiroRechazadoException extends RuntimeException {
    public RetiroRechazadoException(String mensaje) {
        super(mensaje);
    }
}
