package fe.banco_digital.service;

import fe.banco_digital.dto.RegistroNuevoUsuarioRequestDTO;
import fe.banco_digital.dto.RegistroNuevoUsuarioResponseDTO;
import fe.banco_digital.dto.ValidacionIdentidadResponseDTO;
import fe.banco_digital.dto.ValidarIdentidadRequestDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.Rol;
import fe.banco_digital.entity.RolNombre;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.EmailYaExisteException;
import fe.banco_digital.exception.IdentificacionDuplicadaException;
import fe.banco_digital.exception.UsuarioYaExisteException;
import fe.banco_digital.repository.ClienteRepository;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.RolRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroUsuarioServiceImplTest {

    @Mock ClienteRepository clienteRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock CuentaRepository cuentaRepository;
    @Mock RolRepository rolRepository;
    @Mock NumeroCuentaService numeroCuentaService;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks RegistroUsuarioServiceImpl service;

    RegistroNuevoUsuarioRequestDTO dto;

    @BeforeEach
    void setUp() {
        dto = new RegistroNuevoUsuarioRequestDTO();
        dto.setDocumento("1234567890");
        dto.setNombre("Ana García");
        dto.setEmail("ana@example.com");
        dto.setDireccion("Calle 10 #5-20");
        dto.setTelefono("3001234567");
        dto.setUsername("anagarcia");
        dto.setPassword("secret123");
        dto.setFechaExpedicion(LocalDate.of(2010, 1, 1));
    }

    // ── validarIdentidad ─────────────────────────────────────────────────────

    @Test
    void validarIdentidad_documentoLibre_retornaDisponible() {
        when(clienteRepository.existsByDocumento("1234567890")).thenReturn(false);

        ValidarIdentidadRequestDTO req = new ValidarIdentidadRequestDTO();
        req.setDocumento("1234567890");

        ValidacionIdentidadResponseDTO result = service.validarIdentidad(req);

        assertTrue(result.isDisponible());
    }

    @Test
    void validarIdentidad_documentoDuplicado_throws() {
        when(clienteRepository.existsByDocumento("1234567890")).thenReturn(true);

        ValidarIdentidadRequestDTO req = new ValidarIdentidadRequestDTO();
        req.setDocumento("1234567890");

        assertThrows(IdentificacionDuplicadaException.class, () -> service.validarIdentidad(req));
    }

    // ── registrar ────────────────────────────────────────────────────────────

    @Test
    void registrar_exitoso_creaTresEntidadesYRetornaDTO() {
        when(clienteRepository.existsByDocumento(any())).thenReturn(false);
        when(clienteRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByUsername(any())).thenReturn(false);
        when(numeroCuentaService.generarNumeroCuenta()).thenReturn("87654321");
        when(passwordEncoder.encode(any())).thenReturn("hashedPass");

        Cliente clienteGuardado = new Cliente();
        clienteGuardado.setIdCliente(1L);
        when(clienteRepository.save(any())).thenReturn(clienteGuardado);

        Cuenta cuentaGuardada = new Cuenta();
        cuentaGuardada.setIdCuenta(2L);
        cuentaGuardada.setNumeroCuenta("87654321");
        cuentaGuardada.setSaldo(BigDecimal.ZERO);
        when(cuentaRepository.save(any())).thenReturn(cuentaGuardada);

        Rol rolCliente = new Rol();
        rolCliente.setNombre(RolNombre.CLIENTE);
        when(rolRepository.findByNombre(RolNombre.CLIENTE)).thenReturn(Optional.of(rolCliente));

        Usuario usuarioGuardado = new Usuario();
        usuarioGuardado.setIdUsuario(3L);
        when(usuarioRepository.save(any())).thenReturn(usuarioGuardado);

        RegistroNuevoUsuarioResponseDTO result = service.registrar(dto);

        assertNotNull(result);
        assertEquals("87654321", result.getNumeroCuenta());
        verify(clienteRepository).save(any());
        verify(cuentaRepository).save(any());
        verify(usuarioRepository).save(any());
    }

    @Test
    void registrar_documentoDuplicado_throws() {
        when(clienteRepository.existsByDocumento(dto.getDocumento())).thenReturn(true);

        assertThrows(IdentificacionDuplicadaException.class, () -> service.registrar(dto));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void registrar_emailDuplicado_throws() {
        when(clienteRepository.existsByDocumento(any())).thenReturn(false);
        when(clienteRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(EmailYaExisteException.class, () -> service.registrar(dto));
    }

    @Test
    void registrar_usernameDuplicado_throws() {
        when(clienteRepository.existsByDocumento(any())).thenReturn(false);
        when(clienteRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByUsername(dto.getUsername())).thenReturn(true);

        assertThrows(UsuarioYaExisteException.class, () -> service.registrar(dto));
    }

    @Test
    void registrar_rolNoExiste_loCreaNuevo() {
        when(clienteRepository.existsByDocumento(any())).thenReturn(false);
        when(clienteRepository.existsByEmail(any())).thenReturn(false);
        when(usuarioRepository.existsByUsername(any())).thenReturn(false);
        when(numeroCuentaService.generarNumeroCuenta()).thenReturn("11112222");
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        Cliente clienteGuardado = new Cliente();
        clienteGuardado.setIdCliente(1L);
        when(clienteRepository.save(any())).thenReturn(clienteGuardado);

        Cuenta cuentaGuardada = new Cuenta();
        cuentaGuardada.setIdCuenta(2L);
        cuentaGuardada.setNumeroCuenta("11112222");
        cuentaGuardada.setSaldo(BigDecimal.ZERO);
        when(cuentaRepository.save(any())).thenReturn(cuentaGuardada);

        when(rolRepository.findByNombre(RolNombre.CLIENTE)).thenReturn(Optional.empty());
        Rol nuevoRol = new Rol();
        nuevoRol.setNombre(RolNombre.CLIENTE);
        when(rolRepository.save(any())).thenReturn(nuevoRol);

        Usuario usuarioGuardado = new Usuario();
        usuarioGuardado.setIdUsuario(3L);
        when(usuarioRepository.save(any())).thenReturn(usuarioGuardado);

        service.registrar(dto);

        verify(rolRepository).save(any(Rol.class));
    }
}
