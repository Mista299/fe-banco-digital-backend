package fe.banco_digital.service;

import fe.banco_digital.dto.CierreCuentaRespuestaDTO;
import fe.banco_digital.dto.CierreCuentaSolicitudDTO;
import fe.banco_digital.dto.CuentaResumenDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.event.AuditoriaEvent;
import fe.banco_digital.exception.*;
import fe.banco_digital.mapper.CuentaMapper;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CuentaServiceImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CuentaMapper cuentaMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CuentaServiceImpl service;

    private Usuario usuario;
    private Cuenta cuenta;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setUsername("user");
        usuario.setPasswordHash("hash");

        Cliente cliente = new Cliente();
        cliente.setIdCliente(10L);
        usuario.setCliente(cliente);

        cuenta = new Cuenta();
        cuenta.setIdCuenta(1L);
        cuenta.setNumeroCuenta("123");
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldo(BigDecimal.ZERO);
        cuenta.setTipo(TipoCuenta.AHORROS);
    }

    // CASO OK

    @Test
    void cerrarCuenta_success() {
        when(usuarioRepository.findByUsername("user"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(true);

        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(any(), any()))
                .thenReturn(Optional.of(cuenta));

        CierreCuentaSolicitudDTO solicitud = new CierreCuentaSolicitudDTO();
        solicitud.setIdCuenta(1L);
        solicitud.setContrasena("ok");

        CierreCuentaRespuestaDTO respuesta = service.cerrarCuenta(solicitud, "user");

        assertEquals("INACTIVA", respuesta.getEstado());
        verify(cuentaRepository).save(cuenta);
        verify(eventPublisher).publishEvent(any(AuditoriaEvent.class));
    }

    // CONTRASEÑA INCORRECTA

    @Test
    void cerrarCuenta_contrasenaIncorrecta_lanzaExcepcion() {
        when(usuarioRepository.findByUsername("user"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(false);

        CierreCuentaSolicitudDTO solicitud = new CierreCuentaSolicitudDTO();
        solicitud.setContrasena("mal");

        assertThrows(AutenticacionFallidaException.class,
                () -> service.cerrarCuenta(solicitud, "user"));
    }

    // USUARIO NO EXISTE

    @Test
    void cerrarCuenta_usuarioNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByUsername("user"))
                .thenReturn(Optional.empty());

        CierreCuentaSolicitudDTO solicitud = new CierreCuentaSolicitudDTO();

        assertThrows(AutenticacionFallidaException.class,
                () -> service.cerrarCuenta(solicitud, "user"));
    }

    // CUENTA NO EXISTE

    @Test
    void cerrarCuenta_cuentaNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByUsername("user"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(true);

        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(any(), any()))
                .thenReturn(Optional.empty());

        CierreCuentaSolicitudDTO solicitud = new CierreCuentaSolicitudDTO();
        solicitud.setIdCuenta(1L);
        solicitud.setContrasena("ok");

        assertThrows(CuentaNoEncontradaException.class,
                () -> service.cerrarCuenta(solicitud, "user"));
    }

    // CUENTA YA CERRADA

    @Test
    void cerrarCuenta_cuentaYaCerrada_lanzaExcepcion() {
        cuenta.setEstado(EstadoCuenta.INACTIVA);

        when(usuarioRepository.findByUsername("user"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(true);

        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(any(), any()))
                .thenReturn(Optional.of(cuenta));

        CierreCuentaSolicitudDTO solicitud = new CierreCuentaSolicitudDTO();
        solicitud.setIdCuenta(1L);
        solicitud.setContrasena("ok");

        assertThrows(CuentaYaCerradaException.class,
                () -> service.cerrarCuenta(solicitud, "user"));
    }

    // CUENTA CON SALDO

    @Test
    void cerrarCuenta_conSaldo_lanzaExcepcion() {
        cuenta.setSaldo(new BigDecimal("100"));

        when(usuarioRepository.findByUsername("user"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches(any(), any()))
                .thenReturn(true);

        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(any(), any()))
                .thenReturn(Optional.of(cuenta));

        CierreCuentaSolicitudDTO solicitud = new CierreCuentaSolicitudDTO();
        solicitud.setIdCuenta(1L);
        solicitud.setContrasena("ok");

        assertThrows(SaldoPendienteException.class,
                () -> service.cerrarCuenta(solicitud, "user"));
    }

    // OBTENER CUENTAS

    @Test
    void obtenerCuentasDelCliente_success() {
        when(usuarioRepository.findByUsername("user"))
                .thenReturn(Optional.of(usuario));

        when(cuentaRepository.findByCliente_IdCliente(any()))
                .thenReturn(List.of(cuenta));

        CuentaResumenDTO dto = new CuentaResumenDTO(cuenta);

        when(cuentaMapper.aCuentaResumenDTO(any()))
                .thenReturn(dto);

        List<CuentaResumenDTO> resultado = service.obtenerCuentasDelCliente("user");

        assertEquals(1, resultado.size());
        verify(cuentaMapper).aCuentaResumenDTO(cuenta);
    }

    // USUARIO NO EXISTE (OBTENER CUENTAS)

    @Test
    void obtenerCuentasDelCliente_usuarioNoExiste() {
        when(usuarioRepository.findByUsername("user"))
                .thenReturn(Optional.empty());

        assertThrows(AutenticacionFallidaException.class,
                () -> service.obtenerCuentasDelCliente("user"));
    }
}