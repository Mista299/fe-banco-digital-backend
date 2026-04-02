package fe.banco_digital.repository;

import fe.banco_digital.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUserIdAndActiveTrue(Long userId);
}
