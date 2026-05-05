package fe.banco_digital.service;

import fe.banco_digital.dto.ActualizarClienteDTO;

public interface ClienteService {

    void actualizar(ActualizarClienteDTO dto, String username);
}
