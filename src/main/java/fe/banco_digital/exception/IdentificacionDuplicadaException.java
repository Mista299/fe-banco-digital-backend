package fe.banco_digital.exception;

public class IdentificacionDuplicadaException extends RuntimeException {
    public IdentificacionDuplicadaException(String documento) {
        super("El número de identificación " + documento + " ya se encuentra vinculado a una cuenta existente.");
    }
}
