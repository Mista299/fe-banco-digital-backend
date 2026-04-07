package fe.banco_digital.exception;

public class TokenExpiradoException extends RuntimeException {

    public TokenExpiradoException() {
        super("La sesión expiró, inicie sesión nuevamente");
    }
}
