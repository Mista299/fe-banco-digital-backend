package fe.banco_digital.repository;

import fe.banco_digital.entity.TransferenciaInternacional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransferenciaInternacionalRepository extends JpaRepository<TransferenciaInternacional, Long> {

    List<TransferenciaInternacional> findByCuentaOrigen_IdCuentaOrderByFechaDesc(Long idCuenta);

    List<TransferenciaInternacional> findByCuentaOrigen_IdCuentaAndFechaBetweenOrderByFechaDesc(
            Long idCuenta, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
