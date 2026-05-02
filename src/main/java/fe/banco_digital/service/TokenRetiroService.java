package fe.banco_digital.service;

public interface TokenRetiroService {

    String generarToken(Long idCuenta);

    boolean validarToken(String codigo);
}