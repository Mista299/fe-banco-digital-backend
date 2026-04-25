package fe.banco_digital.service;

import fe.banco_digital.dto.ActualizarClienteDTO;

public interface ClienteService {

    void actualizar(Long id, ActualizarClienteDTO dto, String username);
}
