package fe.banco_digital.repository;

import fe.banco_digital.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Cliente, Long> {
}