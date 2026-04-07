package fe.banco_digital.exception;

public class SaldoPendienteException extends RuntimeException {
    public SaldoPendienteException() {
        super("No es posible cerrar la cuenta porque aún tienes saldo disponible, " +
                "por favor retira o traslada tus fondos primero.");
    }
}