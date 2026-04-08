package fe.banco_digital.repository;

import fe.banco_digital.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    @Query("SELECT t FROM Transaccion t WHERE t.cuentaOrigen.idCuenta = :idCuenta OR t.cuentaDestino.idCuenta = :idCuenta ORDER BY t.fecha DESC")
    List<Transaccion> findByCuentaIdOrderByFechaDesc(@Param("idCuenta") Long idCuenta);

    @Query("SELECT t FROM Transaccion t WHERE (t.cuentaOrigen.idCuenta = :idCuenta OR t.cuentaDestino.idCuenta = :idCuenta) AND t.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY t.fecha DESC")
    List<Transaccion> findByCuentaIdAndFechaBetweenOrderByFechaDesc(
            @Param("idCuenta") Long idCuenta,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin);
}