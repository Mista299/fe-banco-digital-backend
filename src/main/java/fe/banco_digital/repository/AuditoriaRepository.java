package fe.banco_digital.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fe.banco_digital.entity.Auditoria;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
}

