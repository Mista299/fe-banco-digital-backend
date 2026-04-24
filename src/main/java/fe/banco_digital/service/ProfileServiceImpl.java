package fe.banco_digital.service;

import fe.banco_digital.dto.ProfileDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.ClienteNoEncontradoException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.mapper.ProfileMapper;
import fe.banco_digital.repository.ClienteRepository;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final UsuarioRepository usuarioRepository;

    public ProfileServiceImpl(ClienteRepository clienteRepository,
                              CuentaRepository cuentaRepository,
                              UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public ProfileDTO getProfile(Long userId, String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        if (!usuario.getCliente().getIdCliente().equals(userId)) {
            throw new AccesoNoAutorizadoException();
        }

        Cliente cliente = clienteRepository.findById(userId)
                .orElseThrow(() -> new ClienteNoEncontradoException(userId));

        Cuenta cuenta = cuentaRepository
                .findFirstByClienteIdClienteAndEstado(userId, EstadoCuenta.ACTIVA)
                .orElseThrow(() -> new CuentaNoEncontradaException(userId));

        return ProfileMapper.toDTO(cliente, cuenta);
    }
}
