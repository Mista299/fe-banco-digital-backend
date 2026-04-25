package fe.banco_digital.exception;

public class EmailYaExisteException extends RuntimeException {
    public EmailYaExisteException(String email) {
        super("El correo " + email + " ya se encuentra registrado en la plataforma.");
    }
}
