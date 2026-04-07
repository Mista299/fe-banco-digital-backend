package fe.banco_digital.exception;

public class AutenticacionFallidaException extends RuntimeException {
    public AutenticacionFallidaException() {
        super("El proceso de cierre ha sido bloqueado por seguridad: contraseña incorrecta.");
    }
}