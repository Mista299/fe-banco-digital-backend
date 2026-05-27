package fe.banco_digital.service;

import fe.banco_digital.dto.ActualizarClienteDTO;
import fe.banco_digital.dto.CuentaResumenDTO;
import fe.banco_digital.dto.DashboardClienteDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.ClienteNoEncontradoException;
import fe.banco_digital.repository.ClienteRepository;
import fe.banco_digital.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public void actualizar(ActualizarClienteDTO dto, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        Long idCliente = usuario.getCliente().getIdCliente();
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ClienteNoEncontradoException(idCliente));

        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());

        clienteRepository.save(cliente);
    }

    @Override
    @Transactional
    public DashboardClienteDTO obtenerDashboard(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        Cliente cliente = usuario.getCliente();
        List<CuentaResumenDTO> cuentas = cliente.getCuentas()
                .stream()
                .filter(c -> c.getEstado() != EstadoCuenta.INACTIVA)
                .map(CuentaResumenDTO::new)
                .collect(Collectors.toList());

        return new DashboardClienteDTO(cliente.getNombre(), cliente.getEmail(), cuentas);
    }
}
