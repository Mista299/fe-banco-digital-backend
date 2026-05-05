package fe.banco_digital.exception;

public class AccesoNoAutorizadoException extends RuntimeException {
    public AccesoNoAutorizadoException() {
        super("No tienes permiso para acceder a este recurso.");
    }
}
