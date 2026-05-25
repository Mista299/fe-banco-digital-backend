package fe.banco_digital.repository;

import fe.banco_digital.entity.TransferenciaExterna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransferenciaExternaRepository extends JpaRepository<TransferenciaExterna, Long> {

    List<TransferenciaExterna> findByCuentaOrigen_IdCuentaOrderByFechaDesc(Long idCuenta);

    List<TransferenciaExterna> findByCuentaOrigen_IdCuentaAndFechaBetweenOrderByFechaDesc(
            Long idCuenta, LocalDateTime fechaInicio, LocalDateTime fechaFin);

}
