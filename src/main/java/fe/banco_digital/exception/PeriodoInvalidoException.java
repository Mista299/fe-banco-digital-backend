package fe.banco_digital.exception;

public class PeriodoInvalidoException extends RuntimeException {
    public PeriodoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
