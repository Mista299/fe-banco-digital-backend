package fe.banco_digital.exception;

public class PeriodoNoDisponibleException extends RuntimeException {
    public PeriodoNoDisponibleException() {
        super("El extracto oficial estará disponible al finalizar el periodo actual");
    }
}
