package fe.banco_digital.service;

import fe.banco_digital.dto.CierreCuentaRespuestaDTO;
import fe.banco_digital.dto.CierreCuentaSolicitudDTO;
import fe.banco_digital.dto.CuentaResumenDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.TipoCuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.exception.CuentaYaCerradaException;
import fe.banco_digital.exception.SaldoPendienteException;
import fe.banco_digital.mapper.CuentaMapper;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"java:S100", "java:S1192"})
@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    @Mock CuentaRepository cuentaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock CuentaMapper cuentaMapper;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks CuentaServiceImpl service;

    Cliente cliente;
    Usuario usuario;
    Cuenta cuenta;
    CierreCuentaSolicitudDTO solicitud;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(5L);

        usuario = new Usuario();
        usuario.setIdUsuario(10L);
        usuario.setUsername("user1");
        usuario.setPasswordHash("hash123");
        usuario.setCliente(cliente);

        cuenta = new Cuenta();
        cuenta.setIdCuenta(15L);
        cuenta.setNumeroCuenta("12345678");
        cuenta.setTipo(TipoCuenta.AHORROS);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldo(BigDecimal.ZERO);
        cuenta.setCliente(cliente);

        solicitud = new CierreCuentaSolicitudDTO();
        solicitud.setIdCuenta(15L);
        solicitud.setContrasena("pass123");
    }

    @Test
    void cerrarCuenta_exitoso_cambiandoEstadoYPublicandoEvento() {
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("pass123", "hash123")).thenReturn(true);
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(15L, 5L)).thenReturn(Optional.of(cuenta));

        CierreCuentaRespuestaDTO result = service.cerrarCuenta(solicitud, "user1");

        assertEquals(EstadoCuenta.INACTIVA, cuenta.getEstado());
        verify(cuentaRepository).save(cuenta);
        verify(eventPublisher).publishEvent(any());
        assertNotNull(result);
    }

    @Test
    void cerrarCuenta_contrasenaInvalida_throws() {
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThrows(AutenticacionFallidaException.class, () -> service.cerrarCuenta(solicitud, "user1"));
        verify(cuentaRepository, never()).save(any());
    }

    @Test
    void cerrarCuenta_cuentaYaCerrada_throws() {
        cuenta.setEstado(EstadoCuenta.INACTIVA);
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(15L, 5L)).thenReturn(Optional.of(cuenta));

        assertThrows(CuentaYaCerradaException.class, () -> service.cerrarCuenta(solicitud, "user1"));
    }

    @Test
    void cerrarCuenta_conSaldoPendiente_throws() {
        cuenta.setSaldo(new BigDecimal("500.00"));
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(15L, 5L)).thenReturn(Optional.of(cuenta));

        assertThrows(SaldoPendienteException.class, () -> service.cerrarCuenta(solicitud, "user1"));
    }

    @Test
    void cerrarCuenta_usuarioNoEncontrado_throws() {
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.empty());

        assertThrows(AutenticacionFallidaException.class, () -> service.cerrarCuenta(solicitud, "user1"));
    }

    @Test
    void cerrarCuenta_cuentaNoEncontrada_throws() {
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(15L, 5L)).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class, () -> service.cerrarCuenta(solicitud, "user1"));
    }

    @Test
    void obtenerCuentasDelCliente_exitoso_retornaLista() {
        CuentaResumenDTO dto = mock(CuentaResumenDTO.class);
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByCliente_IdCliente(5L)).thenReturn(List.of(cuenta));
        when(cuentaMapper.aCuentaResumenDTO(cuenta)).thenReturn(dto);

        List<CuentaResumenDTO> result = service.obtenerCuentasDelCliente("user1");

        assertEquals(1, result.size());
        assertEquals(dto, result.get(0));
    }

    @Test
    void obtenerCuentasDelCliente_usuarioNoEncontrado_throws() {
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.empty());

        assertThrows(AutenticacionFallidaException.class, () -> service.obtenerCuentasDelCliente("user1"));
    }

    // Escenario sin cuentas activas → mensaje con N/A
    @Test
    void obtenerCuentasDelCliente_sinCuentasActivas_retornaMensajeConNA() {
        Usuario usuario = usuarioConCliente(4L);
        Cuenta bloqueada = new Cuenta();
        bloqueada.setIdCuenta(3L);
        bloqueada.setNumeroCuenta("00090001");
        bloqueada.setTipo(TipoCuenta.AHORROS);
        bloqueada.setSaldo(BigDecimal.ZERO);
        bloqueada.setEstado(EstadoCuenta.BLOQUEADA);

        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByCliente_IdCliente(4L)).thenReturn(List.of(bloqueada));

        DashboardResponseDTO resultado = cuentaService.obtenerCuentasDelCliente(USERNAME);

        assertThat(resultado.getMensajeBienvenida()).contains("N/A");
    }

    // ── cerrarCuenta ─────────────────────────────────────────────────────────

    @Test
    void cerrarCuenta_exitoso_cambiaEstadoAInactiva() {
        Usuario usuario = usuarioConCliente(1L);
        usuario.setIdUsuario(5L);
        usuario.setPasswordHash("$2a$10$hash");

        Cuenta cuenta = cuentaActiva(1L, "00010001", BigDecimal.ZERO);
        cuenta.setCliente(usuario.getCliente());

        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("mi-clave", "$2a$10$hash")).thenReturn(true);
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 1L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.save(any())).thenReturn(cuenta);

        CierreCuentaSolicitudDTO solicitud = new CierreCuentaSolicitudDTO();
        solicitud.setIdCuenta(1L);
        solicitud.setContrasena("mi-clave");

        CierreCuentaRespuestaDTO resp = cuentaService.cerrarCuenta(solicitud, USERNAME);

        assertThat(resp).isNotNull();
        assertThat(cuenta.getEstado()).isEqualTo(EstadoCuenta.INACTIVA);
        verify(cuentaRepository).save(cuenta);
    }

    @Test
    void cerrarCuenta_usuarioNoEncontrado_throws() {
        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        CierreCuentaSolicitudDTO solicitud = new CierreCuentaSolicitudDTO();
        solicitud.setIdCuenta(1L);
        solicitud.setContrasena("clave");

        assertThatThrownBy(() -> cuentaService.cerrarCuenta(solicitud, USERNAME))
                .isInstanceOf(AutenticacionFallidaException.class);
    }

    @Test
    void cerrarCuenta_contrasenaInvalida_throws() {
        Usuario usuario = usuarioConCliente(1L);
        usuario.setPasswordHash("$2a$10$hash");

        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave-mala", "$2a$10$hash")).thenReturn(false);

        CierreCuentaSolicitudDTO solicitud = new CierreCuentaSolicitudDTO();
        solicitud.setIdCuenta(1L);
        solicitud.setContrasena("clave-mala");

        assertThatThrownBy(() -> cuentaService.cerrarCuenta(solicitud, USERNAME))
                .isInstanceOf(AutenticacionFallidaException.class);
    }

    @Test
    void cerrarCuenta_cuentaNoEncontrada_throws() {
        Usuario usuario = usuarioConCliente(1L);
        usuario.setPasswordHash("$2a$10$hash");

        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave", "$2a$10$hash")).thenReturn(true);
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 1L)).thenReturn(Optional.empty());

        CierreCuentaSolicitudDTO solicitud = new CierreCuentaSolicitudDTO();
        solicitud.setIdCuenta(1L);
        solicitud.setContrasena("clave");

        assertThatThrownBy(() -> cuentaService.cerrarCuenta(solicitud, USERNAME))
                .isInstanceOf(CuentaNoEncontradaException.class);
    }

    @Test
    void cerrarCuenta_cuentaYaCerrada_throws() {
        Usuario usuario = usuarioConCliente(1L);
        usuario.setPasswordHash("$2a$10$hash");

        Cuenta cuenta = new Cuenta();
        cuenta.setIdCuenta(1L);
        cuenta.setNumeroCuenta("00010001");
        cuenta.setEstado(EstadoCuenta.INACTIVA);

        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave", "$2a$10$hash")).thenReturn(true);
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 1L)).thenReturn(Optional.of(cuenta));

        CierreCuentaSolicitudDTO solicitud = new CierreCuentaSolicitudDTO();
        solicitud.setIdCuenta(1L);
        solicitud.setContrasena("clave");

        assertThatThrownBy(() -> cuentaService.cerrarCuenta(solicitud, USERNAME))
                .isInstanceOf(CuentaYaCerradaException.class);
    }

    @Test
    void cerrarCuenta_saldoPendiente_throws() {
        Usuario usuario = usuarioConCliente(1L);
        usuario.setPasswordHash("$2a$10$hash");

        Cuenta cuenta = cuentaActiva(1L, "00010001", new BigDecimal("5000"));
        cuenta.setCliente(usuario.getCliente());

        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave", "$2a$10$hash")).thenReturn(true);
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 1L)).thenReturn(Optional.of(cuenta));

        CierreCuentaSolicitudDTO solicitud = new CierreCuentaSolicitudDTO();
        solicitud.setIdCuenta(1L);
        solicitud.setContrasena("clave");

        assertThatThrownBy(() -> cuentaService.cerrarCuenta(solicitud, USERNAME))
                .isInstanceOf(SaldoPendienteException.class);
    }
}
