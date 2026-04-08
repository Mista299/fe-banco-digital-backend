package fe.banco_digital.repository;

import fe.banco_digital.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByCuentaOrigenIdCuentaOrderByFechaDesc(Long idCuenta);

    List<Transaccion> findByCuentaOrigenIdCuentaAndFechaBetweenOrderByFechaDesc(
            Long idCuenta,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin);
}