package fe.banco_digital.exception;

public class UsuarioYaExisteException extends RuntimeException {

    public UsuarioYaExisteException(String username) {
        super("El nombre de usuario '" + username + "' ya está en uso");
    }
}
