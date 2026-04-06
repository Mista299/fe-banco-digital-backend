package fe.banco_digital.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fe.banco_digital.entity.Rol;
import fe.banco_digital.entity.RolNombre;

public interface RolRepository extends JpaRepository<Rol, Long> {
	Optional<Rol> findByNombre(RolNombre nombre);
}

