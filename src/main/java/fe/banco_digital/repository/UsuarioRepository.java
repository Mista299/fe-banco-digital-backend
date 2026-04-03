package fe.banco_digital.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fe.banco_digital.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	Optional<Usuario> findByUsername(String username);
}

