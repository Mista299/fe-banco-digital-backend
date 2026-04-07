package fe.banco_digital.exception;

public class TokenInvalidoException extends RuntimeException {

    public TokenInvalidoException() {
        super("El token no es válido o fue revocado");
    }
}
