package fe.banco_digital.service;

import fe.banco_digital.entity.Account;
import fe.banco_digital.entity.AccountBlockLog;
import fe.banco_digital.entity.AccountStatus;
import fe.banco_digital.repository.AccountBlockLogRepository;
import fe.banco_digital.repository.AccountRepository;
import fe.banco_digital.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AccountSecurityServiceImpl implements AccountSecurityService {

    private final UserRepository userRepo;
    private final AccountRepository accRepo;
    private final AccountBlockLogRepository logRepo;

    public AccountSecurityServiceImpl(UserRepository userRepo,
                                      AccountRepository accRepo,
                                      AccountBlockLogRepository logRepo) {
        this.userRepo = userRepo;
        this.accRepo = accRepo;
        this.logRepo = logRepo;
    }

   
@Override
public void blockAccount(Long userId, String password) {

    // 1️⃣ Validar que el usuario exista
    userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // 2️⃣ Validar contraseña (simulada)
    if (!"1234".equals(password)) {
        throw new RuntimeException("Contraseña incorrecta");
    }

    // 3️⃣ Buscar cuenta activa
    Account account = accRepo
            .findByUserIdAndStatus(userId, AccountStatus.ACTIVE)
            .orElseThrow(() ->
                    new RuntimeException("Cuenta activa no encontrada"));

    // 4️⃣ Bloquear cuenta
    account.setStatus(AccountStatus.BLOCKED);
    accRepo.save(account);

    // 5️⃣ Registrar auditoría
    AccountBlockLog log = new AccountBlockLog();
    log.setAccount(account);
    log.setChannel("APP_MOVIL");
    logRepo.save(log);

    // 6️⃣ Simulación de notificación push
    System.out.println(
            "🔔 Notificación: Cuenta bloqueada el " +
            log.getTimestamp() + " vía APP_MOVIL"
    );
}
@Override
public void unlockAccount(Long userId, String password) {

    // 1️⃣ Validar usuario
    userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // 2️⃣ Validar contraseña (simulada)
    if (!"1234".equals(password)) {
        throw new RuntimeException("Contraseña incorrecta");
    }

    // 3️⃣ Buscar cuenta BLOQUEADA
    Account account = accRepo
            .findByUserIdAndStatus(userId, AccountStatus.BLOCKED)
            .orElseThrow(() ->
                    new RuntimeException("No existe una cuenta bloqueada"));

    // 4️⃣ Desbloquear cuenta
    account.setStatus(AccountStatus.ACTIVE);
    accRepo.save(account);

    // 5️⃣ Registrar auditoría (opcional, recomendado)
    AccountBlockLog log = new AccountBlockLog();
    log.setAccount(account);
    log.setChannel("DESBLOQUEO_APP");
    logRepo.save(log);

    // 6️⃣ Notificación simulada
    System.out.println(
            "🔔 Notificación: Cuenta desbloqueada el "
            + log.getTimestamp() + " vía APP_MOVIL"
    );
} 
}