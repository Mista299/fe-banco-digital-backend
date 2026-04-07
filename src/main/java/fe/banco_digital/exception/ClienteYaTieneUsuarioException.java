package fe.banco_digital.exception;

public class ClienteYaTieneUsuarioException extends RuntimeException {

    public ClienteYaTieneUsuarioException(Long idCliente) {
        super("El cliente con id " + idCliente + " ya tiene un usuario asignado");
    }
}
