package fe.banco_digital.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    Optional<Cuenta> findFirstByClienteIdClienteAndEstado(Long idCliente, EstadoCuenta estado);

    List<Cuenta> findByCliente_IdCliente(Long idCliente);

    Optional<Cuenta> findByIdCuentaAndCliente_IdCliente(Long idCuenta, Long idCliente);

    boolean existsByNumeroCuenta(String numeroCuenta);
}
