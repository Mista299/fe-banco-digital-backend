package fe.banco_digital.repository;

import fe.banco_digital.entity.Cuenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findFirstByClienteIdClienteAndEstado(Long idCliente, fe.banco_digital.entity.EstadoCuenta estado);
}
