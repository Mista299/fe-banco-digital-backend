package fe.banco_digital.repository;

import fe.banco_digital.entity.AccountBlockLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountBlockLogRepository
        extends JpaRepository<AccountBlockLog, Long> {
}
