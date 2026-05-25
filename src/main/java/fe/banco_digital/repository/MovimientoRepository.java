package fe.banco_digital.repository;

import fe.banco_digital.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByCuenta_IdCuentaOrderByFechaDesc(Long idCuenta);

    List<Movimiento> findByCuenta_IdCuentaAndFechaBetweenOrderByFechaDesc(
            Long idCuenta, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Movimiento> findByFechaBetweenOrderByFechaDesc(
            LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
