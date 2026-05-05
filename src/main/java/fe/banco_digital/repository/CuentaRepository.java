package fe.banco_digital.repository;

import java.util.List;
import java.util.Optional;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cuenta c WHERE c.idCuenta = :idCuenta")
    Optional<Cuenta> findByIdCuentaConLock(@Param("idCuenta") Long idCuenta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cuenta c WHERE c.numeroCuenta = :numeroCuenta")
    Optional<Cuenta> findByNumeroCuentaConLock(@Param("numeroCuenta") String numeroCuenta);

    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    Optional<Cuenta> findFirstByClienteIdClienteAndEstado(Long idCliente, EstadoCuenta estado);

    List<Cuenta> findByCliente_IdCliente(Long idCliente);

    Optional<Cuenta> findByIdCuentaAndCliente_IdCliente(Long idCuenta, Long idCliente);

    boolean existsByNumeroCuenta(String numeroCuenta);

    Optional<Cuenta> findByNumeroCuentaAndCliente_IdCliente(String numeroCuenta, Long idCliente);
}
