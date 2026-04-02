package fe.banco_digital.repository;

import fe.banco_digital.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Buscar usuario por número de identificación (opcional, útil)
    Optional<User> findByNumeroIdentificacion(String numeroIdentificacion);
}
