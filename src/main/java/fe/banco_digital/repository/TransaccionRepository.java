package fe.banco_digital.repository;

import fe.banco_digital.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransaccionRepository
        extends JpaRepository<Transaccion, Long> {

    List<Transaccion> findByFechaBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );
}