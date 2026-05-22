package fe.banco_digital.service;

import fe.banco_digital.dto.LoginRequestDTO;
import fe.banco_digital.dto.LoginResponseDTO;
import fe.banco_digital.dto.RegistroRequestDTO;
import fe.banco_digital.dto.UsuarioRegistradoDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.RefreshToken;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.ClienteNoEncontradoException;
import fe.banco_digital.exception.ClienteYaTieneUsuarioException;
import fe.banco_digital.exception.CredencialesInvalidasException;
import fe.banco_digital.exception.UsuarioYaExisteException;
import fe.banco_digital.mapper.AutenticacionMapper;
import fe.banco_digital.mapper.UsuarioMapper;
import fe.banco_digital.repository.ClienteRepository;
import fe.banco_digital.repository.UsuarioRepository;
import fe.banco_digital.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacionServiceImpl implements AutenticacionService {

    private final AuthenticationManager gestorAutenticacion;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder codificadorPassword;
    private final UsuarioMapper usuarioMapper;
    private final AutenticacionMapper autenticacionMapper;

    public AutenticacionServiceImpl(
            AuthenticationManager gestorAutenticacion,
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService,
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            PasswordEncoder codificadorPassword,
            UsuarioMapper usuarioMapper,
            AutenticacionMapper autenticacionMapper) {
        this.gestorAutenticacion = gestorAutenticacion;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.codificadorPassword = codificadorPassword;
        this.usuarioMapper = usuarioMapper;
        this.autenticacionMapper = autenticacionMapper;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO dto) {
        try {
            Authentication autenticacion = gestorAutenticacion.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );

            String username = autenticacion.getName();
            Usuario usuario = usuarioRepository.findByUsername(username).orElseThrow();

            String accessToken = jwtUtil.generarToken(username);
            RefreshToken refreshToken = refreshTokenService.crearRefreshToken(usuario.getIdUsuario());

            return autenticacionMapper.aLoginResponseDTO(accessToken, refreshToken);

        } catch (LockedException ex) {
            throw new CredencialesInvalidasException();
        } catch (BadCredentialsException ex) {
            throw new CredencialesInvalidasException();
        }
    }

    @Override
    @Transactional
    public UsuarioRegistradoDTO registrar(RegistroRequestDTO dto) {
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new UsuarioYaExisteException(dto.getUsername());
        }

        Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                .orElseThrow(() -> new ClienteNoEncontradoException(dto.getIdCliente()));

        if (usuarioRepository.existsByCliente_IdCliente(dto.getIdCliente())) {
            throw new ClienteYaTieneUsuarioException(dto.getIdCliente());
        }

        Usuario usuario = usuarioMapper.aEntidad(dto);
        usuario.setPasswordHash(codificadorPassword.encode(dto.getPassword()));
        usuario.setCliente(cliente);

        Usuario guardado = usuarioRepository.save(usuario);
        return usuarioMapper.aDTO(guardado);
    }
}
