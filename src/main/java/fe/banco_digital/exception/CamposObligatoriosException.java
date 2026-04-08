package fe.banco_digital.exception;

public class CamposObligatoriosException extends RuntimeException {
    public CamposObligatoriosException() {
        super("Los campos faltantes son obligatorios para continuar.");
    }
}
