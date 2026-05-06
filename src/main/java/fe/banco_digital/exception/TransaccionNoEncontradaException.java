package fe.banco_digital.exception;

public class TransaccionNoEncontradaException extends RuntimeException {
    public TransaccionNoEncontradaException(Long idTransaccion) {
        super("Transacción con id " + idTransaccion + " no encontrada.");
    }
}
