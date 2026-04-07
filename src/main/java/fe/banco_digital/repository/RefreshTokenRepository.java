package fe.banco_digital.repository;

import fe.banco_digital.entity.RefreshToken;
import fe.banco_digital.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUsuario(Usuario usuario);
}
