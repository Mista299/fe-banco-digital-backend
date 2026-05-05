package fe.banco_digital.service;

import fe.banco_digital.dto.ActualizarClienteDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.ClienteNoEncontradoException;
import fe.banco_digital.repository.ClienteRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente cliente;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Cliente Test");
        cliente.setDocumento("5555");
        cliente.setEmail("old@example.com");
        cliente.setTelefono("3000000000");

        usuario = new Usuario();
        usuario.setIdUsuario(10L);
        usuario.setUsername("testuser");
        usuario.setCliente(cliente);
    }

    @Test
    void actualizar_updatesCliente_whenExists() {
        ActualizarClienteDTO dto = new ActualizarClienteDTO();
        dto.setEmail("new@example.com");
        dto.setTelefono("3111111111");

        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(cliente));

        clienteService.actualizar(dto, "testuser");

        verify(clienteRepository).save(cliente);
    }

    @Test
    void actualizar_throws_whenClienteNotFound() {
        ActualizarClienteDTO dto = new ActualizarClienteDTO();
        dto.setEmail("new@example.com");
        dto.setTelefono("3111111111");

        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ClienteNoEncontradoException.class, () -> clienteService.actualizar(dto, "testuser"));
    }
}
