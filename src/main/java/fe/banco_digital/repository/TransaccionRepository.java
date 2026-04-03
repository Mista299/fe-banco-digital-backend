package fe.banco_digital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fe.banco_digital.entity.Transaccion;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
}

