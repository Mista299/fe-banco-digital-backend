package fe.banco_digital.service;

import fe.banco_digital.dto.ActualizarClienteDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.exception.ClienteNoEncontradoException;
import fe.banco_digital.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Cliente Test");
        cliente.setDocumento("5555");
        cliente.setEmail("old@example.com");
        cliente.setTelefono("3000000000");
    }

    @Test
    void actualizar_updatesCliente_whenExists() {
        ActualizarClienteDTO dto = new ActualizarClienteDTO();
        dto.setEmail("new@example.com");
        dto.setTelefono("3111111111");

        doReturn(Optional.of(cliente)).when(clienteRepository).findById(anyLong());

        clienteService.actualizar(1L, dto);

        verify(clienteRepository).save(cliente);
    }

    @Test
    void actualizar_throws_whenClienteNotFound() {
        ActualizarClienteDTO dto = new ActualizarClienteDTO();
        dto.setEmail("new@example.com");
        dto.setTelefono("3111111111");

        doReturn(Optional.empty()).when(clienteRepository).findById(anyLong());

        assertThrows(ClienteNoEncontradoException.class, () -> clienteService.actualizar(1L, dto));
    }
}
