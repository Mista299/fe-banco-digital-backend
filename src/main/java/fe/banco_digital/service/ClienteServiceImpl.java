package fe.banco_digital.service;

import fe.banco_digital.dto.ActualizarClienteDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.exception.ClienteNoEncontradoException;
import fe.banco_digital.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional
    public void actualizar(Long id, ActualizarClienteDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNoEncontradoException(id));

        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());

        clienteRepository.save(cliente);
    }
}
