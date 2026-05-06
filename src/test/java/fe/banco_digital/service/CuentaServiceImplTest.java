package fe.banco_digital.service;

import fe.banco_digital.dto.CierreCuentaRespuestaDTO;
import fe.banco_digital.dto.CierreCuentaSolicitudDTO;
import fe.banco_digital.dto.CuentaResumenDTO;
import fe.banco_digital.dto.DashboardResponseDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.exception.CuentaYaCerradaException;
import fe.banco_digital.exception.SaldoPendienteException;
import fe.banco_digital.mapper.CuentaMapper;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"java:S100", "java:S1192"})
@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    private static final String USERNAME = "testuser";

    @Mock CuentaRepository cuentaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Spy  CuentaMapper cuentaMapper;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks CuentaServiceImpl cuentaService;

    private Usuario usuarioConCliente(Long idCliente) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(idCliente);

        Usuario usuario = new Usuario();
        usuario.setUsername(USERNAME);
        usuario.setCliente(cliente);
        return usuario;
    }

    private Cuenta cuentaActiva(Long id, String numero, BigDecimal saldo) {
        Cuenta c = new Cuenta();
        c.setIdCuenta(id);
        c.setNumeroCuenta(numero);
        c.setTipo(TipoCuenta.AHORROS);
        c.setSaldo(saldo);
        c.setEstado(EstadoCuenta.ACTIVA);
        return c;
    }

    // Escenario 1: múltiples cuentas → lista con números enmascarados
    @Test
    void obtenerCuentasDelCliente_retornaListaEnmascarada_cuandoTieneVariasCuentas() {
        Usuario usuario = usuarioConCliente(1L);
        List<Cuenta> cuentas = List.of(
                cuentaActiva(1L, "00010001", new BigDecimal("150000")),
                cuentaActiva(2L, "00020002", new BigDecimal("75000"))
        );

        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByCliente_IdCliente(1L)).thenReturn(cuentas);

        DashboardResponseDTO resultado = cuentaService.obtenerCuentasDelCliente(USERNAME);

        assertThat(resultado.getCuentas()).hasSize(2);
        assertThat(resultado.getCuentas().get(0).getNumeroEnmascarado()).isEqualTo("****0001");
        assertThat(resultado.getCuentas().get(0).getSaldoDisponible()).isNotNull();
        assertThat(resultado.getCuentas().get(0).isPermiteTransacciones()).isTrue();
        assertThat(resultado.getCuentas().get(1).getNumeroEnmascarado()).isEqualTo("****0002");
        assertThat(resultado.getMensajeBienvenida()).isNotBlank();
    }

    // Escenario 2: una sola cuenta → lista con un elemento
    @Test
    void obtenerCuentasDelCliente_retornaUnElemento_cuandoTieneUnaCuenta() {
        Usuario usuario = usuarioConCliente(2L);
        List<Cuenta> cuentas = List.of(cuentaActiva(1L, "00010001", new BigDecimal("200000")));

        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByCliente_IdCliente(2L)).thenReturn(cuentas);

        DashboardResponseDTO resultado = cuentaService.obtenerCuentasDelCliente(USERNAME);

        assertThat(resultado.getCuentas()).hasSize(1);
        assertThat(resultado.getCuentas().get(0).getSaldo()).isEqualByComparingTo("200000");
    }

    // Escenario 3: cuenta inactiva → etiqueta y transacciones bloqueadas
    @Test
    void obtenerCuentasDelCliente_marcaCuentaCerrada_cuandoEstadoEsInactiva() {
        Usuario usuario = usuarioConCliente(3L);
        Cuenta cerrada = new Cuenta();
        cerrada.setIdCuenta(2L);
        cerrada.setNumeroCuenta("00050001");
        cerrada.setTipo(TipoCuenta.AHORROS);
        cerrada.setSaldo(BigDecimal.ZERO);
        cerrada.setEstado(EstadoCuenta.INACTIVA);

        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByCliente_IdCliente(3L)).thenReturn(List.of(cerrada));

        DashboardResponseDTO resultado = cuentaService.obtenerCuentasDelCliente(USERNAME);

        assertThat(resultado.getCuentas().get(0).getEtiquetaVisual()).isEqualTo("Cuenta Cerrada");
        assertThat(resultado.getCuentas().get(0).isPermiteTransacciones()).isFalse();
    }

    // Usuario no existe → lanza excepción de autenticación
    @Test
    void obtenerCuentasDelCliente_lanzaExcepcion_cuandoUsuarioNoExiste() {
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaService.obtenerCuentasDelCliente("fantasma"))
                .isInstanceOf(AutenticacionFallidaException.class);
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
