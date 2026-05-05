package fe.banco_digital.service;

import fe.banco_digital.dto.RegistroNuevoUsuarioRequestDTO;
import fe.banco_digital.dto.RegistroNuevoUsuarioResponseDTO;
import fe.banco_digital.dto.ValidacionIdentidadResponseDTO;
import fe.banco_digital.dto.ValidarIdentidadRequestDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.EstadoUsuario;
import fe.banco_digital.entity.Rol;
import fe.banco_digital.entity.RolNombre;
import fe.banco_digital.entity.TipoCuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.EmailYaExisteException;
import fe.banco_digital.exception.IdentificacionDuplicadaException;
import fe.banco_digital.exception.UsuarioYaExisteException;
import fe.banco_digital.repository.ClienteRepository;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.RolRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class RegistroUsuarioServiceImpl implements RegistroUsuarioService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final RolRepository rolRepository;
    private final NumeroCuentaService numeroCuentaService;
    private final PasswordEncoder passwordEncoder;

    public RegistroUsuarioServiceImpl(ClienteRepository clienteRepository,
                                      UsuarioRepository usuarioRepository,
                                      CuentaRepository cuentaRepository,
                                      RolRepository rolRepository,
                                      NumeroCuentaService numeroCuentaService,
                                      PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.cuentaRepository = cuentaRepository;
        this.rolRepository = rolRepository;
        this.numeroCuentaService = numeroCuentaService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ValidacionIdentidadResponseDTO validarIdentidad(ValidarIdentidadRequestDTO dto) {
        if (clienteRepository.existsByDocumento(dto.getDocumento())) {
            throw new IdentificacionDuplicadaException(dto.getDocumento());
        }
        return new ValidacionIdentidadResponseDTO(true, "Identidad disponible para continuar con el registro.");
    }

    @Override
    @Transactional
    public RegistroNuevoUsuarioResponseDTO registrar(RegistroNuevoUsuarioRequestDTO dto) {
        if (clienteRepository.existsByDocumento(dto.getDocumento())) {
            throw new IdentificacionDuplicadaException(dto.getDocumento());
        }

        if (clienteRepository.existsByEmail(dto.getEmail())) {
            throw new EmailYaExisteException(dto.getEmail());
        }

        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new UsuarioYaExisteException(dto.getUsername());
        }

        Cliente cliente = new Cliente();
        cliente.setDocumento(dto.getDocumento());
        cliente.setFechaExpedicion(dto.getFechaExpedicion());
        cliente.setNombre(dto.getNombre());
        cliente.setEmail(dto.getEmail());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setFechaRegistro(LocalDateTime.now());
        Cliente clienteGuardado = clienteRepository.save(cliente);

        Cuenta cuenta = new Cuenta();
        cuenta.setCliente(clienteGuardado);
        cuenta.setNumeroCuenta(numeroCuentaService.generarNumeroCuenta());
        cuenta.setTipo(TipoCuenta.AHORROS);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldo(BigDecimal.ZERO);
        Cuenta cuentaGuardada = cuentaRepository.save(cuenta);

        Rol rolCliente = rolRepository.findByNombre(RolNombre.CLIENTE)
                .orElseGet(() -> {
                    Rol rol = new Rol();
                    rol.setNombre(RolNombre.CLIENTE);
                    return rolRepository.save(rol);
                });

        Usuario usuario = new Usuario();
        usuario.setCliente(clienteGuardado);
        usuario.setUsername(dto.getUsername());
        usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        usuario.setEstado(EstadoUsuario.ACTIVO);
        usuario.setRoles(Set.of(rolCliente));
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        RegistroNuevoUsuarioResponseDTO response = new RegistroNuevoUsuarioResponseDTO();
        response.setIdCliente(clienteGuardado.getIdCliente());
        response.setIdUsuario(usuarioGuardado.getIdUsuario());
        response.setIdCuenta(cuentaGuardada.getIdCuenta());
        response.setNumeroCuenta(cuentaGuardada.getNumeroCuenta());
        response.setSaldo(cuentaGuardada.getSaldo());
        response.setEstadoCuenta(cuentaGuardada.getEstado().name());
        response.setMensaje("Cliente registrado exitosamente, junto al número de cuenta y saldo.");
        return response;
    }
}
