package fe.banco_digital.repository;

import fe.banco_digital.entity.EstadoToken;
import fe.banco_digital.entity.TokenRetiro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRetiroRepository extends JpaRepository<TokenRetiro, Long> {

    Optional<TokenRetiro> findByCodigo(String codigo);

    List<TokenRetiro> findByCuenta_IdCuentaAndEstado(Long idCuenta, EstadoToken estado);
}