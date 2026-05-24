package fe.banco_digital.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.TipoCuenta;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cuenta c WHERE c.idCuenta = :idCuenta")
    Optional<Cuenta> findByIdCuentaConLock(@Param("idCuenta") Long idCuenta);
    Optional<Cuenta> findByNumeroCuenta(String numeroCuenta);

    Optional<Cuenta> findFirstByClienteIdClienteAndEstado(Long idCliente, EstadoCuenta estado);

    List<Cuenta> findByCliente_IdClienteOrderByIdCuentaAsc(Long idCliente);

    Optional<Cuenta> findByIdCuentaAndCliente_IdCliente(Long idCuenta, Long idCliente);

    boolean existsByNumeroCuenta(String numeroCuenta);

    @Query("SELECT COUNT(c) FROM Cuenta c WHERE c.cliente.idCliente = :idCliente AND c.estado IN :estados")
    long countByClienteIdClienteAndEstadoIn(
            @Param("idCliente") Long idCliente,
            @Param("estados") List<EstadoCuenta> estados);

    @Query("SELECT COUNT(c) FROM Cuenta c WHERE c.cliente.idCliente = :idCliente AND c.tipo = :tipo AND c.estado IN :estados")
    long countByClienteIdClienteAndTipoAndEstadoIn(
            @Param("idCliente") Long idCliente,
            @Param("tipo") TipoCuenta tipo,
            @Param("estados") List<EstadoCuenta> estados);

    @Query("SELECT c FROM Cuenta c JOIN FETCH c.cliente WHERE c.estado = :estado")
    List<Cuenta> findByEstadoConCliente(@Param("estado") EstadoCuenta estado);

    @Query("SELECT COALESCE(SUM(c.saldo),0) FROM Cuenta c")
    BigDecimal obtenerSaldoTotalSistema();

    @Query("SELECT COALESCE(SUM(c.saldo),0) FROM Cuenta c WHERE c.tipo = :tipo")
    BigDecimal obtenerSaldoPorTipo(@Param("tipo") TipoCuenta tipo);

    List<Cuenta> findBySaldoBetween(BigDecimal min, BigDecimal max);

    List<Cuenta> findBySaldoGreaterThan(BigDecimal min);
}
