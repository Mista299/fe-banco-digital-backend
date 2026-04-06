package fe.banco_digital.service;

public interface AccountSecurityService {

    void bloquearCuenta(Long idUsuario, String password);

    void desbloquearCuenta(Long idUsuario, String password);
}
