
package fe.banco_digital.service;

public interface AccountSecurityService {

    void blockAccount(Long userId, String password);

    void unlockAccount(Long userId, String password);
}
