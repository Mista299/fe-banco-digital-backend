package fe.banco_digital.service;

public interface AccountSecurityService {

    void bloquearCuenta(String username, String password);

    void desbloquearCuenta(String username, String password);
}
