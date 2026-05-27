package fe.banco_digital.repository;

import fe.banco_digital.entity.DepositoPendiente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DepositoPendienteRepository extends JpaRepository<DepositoPendiente, Long> {
    Optional<DepositoPendiente> findByReferenciaGateway(String referenciaGateway);
}
