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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutenticacionServiceImplTest {

    @Mock
    AuthenticationManager gestorAutenticacion;

    @Mock
    JwtUtil jwtUtil;

    @Mock
    RefreshTokenService refreshTokenService;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    ClienteRepository clienteRepository;

    @Mock
    PasswordEncoder codificadorPassword;

    @Mock
    UsuarioMapper usuarioMapper;

    @Mock
    AutenticacionMapper autenticacionMapper;

    @InjectMocks
    AutenticacionServiceImpl service;

    @BeforeEach
    void setUp() {
    }

    @Test
    void login_success_returnsDto() {
        LoginRequestDTO req = new LoginRequestDTO();
        req.setUsername("user1");
        req.setPassword("pwd");

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user1");

        when(gestorAutenticacion.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(5L);
        usuario.setUsername("user1");

        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(jwtUtil.generarToken("user1")).thenReturn("token-abc");

        RefreshToken rt = new RefreshToken();
        rt.setToken("rftoken");
        when(refreshTokenService.crearRefreshToken(5L)).thenReturn(rt);

        LoginResponseDTO out = new LoginResponseDTO();
        out.setAccessToken("token-abc");
        when(autenticacionMapper.aLoginResponseDTO("token-abc", rt)).thenReturn(out);

        LoginResponseDTO res = service.login(req);

        assertNotNull(res);
        assertEquals("token-abc", res.getAccessToken());
        verify(gestorAutenticacion).authenticate(any());
    }

    @Test
    void login_badCredentials_throws() {
        LoginRequestDTO req = new LoginRequestDTO();
        req.setUsername("u"); req.setPassword("p");

        when(gestorAutenticacion.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(CredencialesInvalidasException.class, () -> service.login(req));
    }

    @Test
    void registrar_success_returnsDto() {
        RegistroRequestDTO dto = new RegistroRequestDTO();
        dto.setUsername("newuser"); dto.setPassword("p"); dto.setIdCliente(11L);

        when(usuarioRepository.existsByUsername("newuser")).thenReturn(false);

        Cliente cliente = new Cliente();
        cliente.setIdCliente(11L);
        when(clienteRepository.findById(11L)).thenReturn(Optional.of(cliente));

        when(usuarioRepository.existsByCliente_IdCliente(11L)).thenReturn(false);

        Usuario entidad = new Usuario();
        when(usuarioMapper.aEntidad(dto)).thenReturn(entidad);
        when(codificadorPassword.encode("p")).thenReturn("hash");

        Usuario saved = new Usuario();
        saved.setIdUsuario(99L);
        when(usuarioRepository.save(entidad)).thenReturn(saved);

        UsuarioRegistradoDTO expected = new UsuarioRegistradoDTO();
        expected.setIdUsuario(99L);
        when(usuarioMapper.aDTO(saved)).thenReturn(expected);

        UsuarioRegistradoDTO out = service.registrar(dto);

        assertNotNull(out);
        assertEquals(99L, out.getIdUsuario());
    }

    @Test
    void registrar_existingUsername_throws() {
        RegistroRequestDTO dto = new RegistroRequestDTO(); dto.setUsername("u1"); dto.setIdCliente(1L);
        when(usuarioRepository.existsByUsername("u1")).thenReturn(true);
        assertThrows(UsuarioYaExisteException.class, () -> service.registrar(dto));
    }

    @Test
    void registrar_clienteMissing_throws() {
        RegistroRequestDTO dto = new RegistroRequestDTO(); dto.setUsername("u1"); dto.setIdCliente(55L);
        when(usuarioRepository.existsByUsername("u1")).thenReturn(false);
        when(clienteRepository.findById(55L)).thenReturn(Optional.empty());
        assertThrows(ClienteNoEncontradoException.class, () -> service.registrar(dto));
    }

    @Test
    void registrar_clienteYaTieneUsuario_throws() {
        RegistroRequestDTO dto = new RegistroRequestDTO(); dto.setUsername("u1"); dto.setIdCliente(2L);
        when(usuarioRepository.existsByUsername("u1")).thenReturn(false);
        Cliente cliente = new Cliente(); cliente.setIdCliente(2L);
        when(clienteRepository.findById(2L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.existsByCliente_IdCliente(2L)).thenReturn(true);
        assertThrows(ClienteYaTieneUsuarioException.class, () -> service.registrar(dto));
    }
}
