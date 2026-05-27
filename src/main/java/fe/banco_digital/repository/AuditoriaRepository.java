package fe.banco_digital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fe.banco_digital.entity.Auditoria;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findAllByOrderByFechaDesc();
}

