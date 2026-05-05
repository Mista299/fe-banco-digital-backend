package fe.banco_digital.repository;

import fe.banco_digital.entity.TokenRetiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRetiroRepository extends JpaRepository<TokenRetiro, Long> {

    Optional<TokenRetiro> findByCodigo(String codigo);
}