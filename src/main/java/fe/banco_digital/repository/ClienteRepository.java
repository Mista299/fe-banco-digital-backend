package fe.banco_digital.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fe.banco_digital.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
	Optional<Cliente> findByDocumento(String documento);
}

