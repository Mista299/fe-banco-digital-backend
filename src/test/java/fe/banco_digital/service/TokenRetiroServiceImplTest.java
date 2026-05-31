package fe.banco_digital.service;

import fe.banco_digital.dto.GenerarTokenRetiroSolicitudDTO;
import fe.banco_digital.dto.TokenRetiroEstadoDTO;
import fe.banco_digital.dto.TokenRetiroRespuestaDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.OperacionNoPermitidaException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.MovimientoRepository;
import fe.banco_digital.repository.TokenRetiroRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("SameParameterValue")

@ExtendWith(MockitoExtension.class)
class TokenRetiroServiceImplTest {

    @Mock CuentaRepository cuentaRepository;
    @Mock TokenRetiroRepository tokenRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock MovimientoRepository movimientoRepository;

    @InjectMocks TokenRetiroServiceImpl service;

    Cliente cliente;
    Usuario usuario;
    Cuenta cuenta;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1L);

        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setUsername("user1");
        usuario.setCliente(cliente);

        cuenta = new Cuenta();
        cuenta.setIdCuenta(1L);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldo(new BigDecimal("500.00"));
        cuenta.setCliente(cliente);
    }

    private GenerarTokenRetiroSolicitudDTO crearSolicitud(Long idCuenta, BigDecimal monto) {
        try {
            GenerarTokenRetiroSolicitudDTO dto = new GenerarTokenRetiroSolicitudDTO();
            Field fId = GenerarTokenRetiroSolicitudDTO.class.getDeclaredField("idCuenta");
            Field fMonto = GenerarTokenRetiroSolicitudDTO.class.getDeclaredField("monto");
            fId.setAccessible(true); fMonto.setAccessible(true);
            fId.set(dto, idCuenta); fMonto.set(dto, monto);
            return dto;
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @Test
    void generarToken_exitoso() {
        GenerarTokenRetiroSolicitudDTO dto = crearSolicitud(1L, new BigDecimal("100.00"));

        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 1L)).thenReturn(Optional.of(cuenta));
        when(tokenRepository.findByCuenta_IdCuentaAndEstado(1L, EstadoToken.ACTIVO)).thenReturn(Collections.emptyList());
        when(tokenRepository.save(any())).thenAnswer(inv -> {
            TokenRetiro t = inv.getArgument(0);
            t.setFechaExpiracion(LocalDateTime.now().plusMinutes(30));
            return t;
        });

        TokenRetiroRespuestaDTO resultado = service.generarToken(dto, "user1");

        assertNotNull(resultado);
        assertEquals(new BigDecimal("400.00"), cuenta.getSaldo());
        verify(cuentaRepository).save(cuenta);
    }

    @Test
    void generarToken_usuarioNoEncontrado_throws() {
        GenerarTokenRetiroSolicitudDTO dto = crearSolicitud(1L, new BigDecimal("100.00"));
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.empty());
        assertThrows(AutenticacionFallidaException.class, () -> service.generarToken(dto, "user1"));
    }

    @Test
    void generarToken_cuentaNoPertenece_throws() {
        GenerarTokenRetiroSolicitudDTO dto = crearSolicitud(1L, new BigDecimal("100.00"));
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(AccesoNoAutorizadoException.class, () -> service.generarToken(dto, "user1"));
    }

    @Test
    void generarToken_cuentaNoActiva_throws() {
        cuenta.setEstado(EstadoCuenta.BLOQUEADA);
        GenerarTokenRetiroSolicitudDTO dto = crearSolicitud(1L, new BigDecimal("100.00"));
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 1L)).thenReturn(Optional.of(cuenta));
        assertThrows(OperacionNoPermitidaException.class, () -> service.generarToken(dto, "user1"));
    }

    @Test
    void generarToken_montoInvalido_throws() {
        GenerarTokenRetiroSolicitudDTO dto = crearSolicitud(1L, BigDecimal.ZERO);
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 1L)).thenReturn(Optional.of(cuenta));
        assertThrows(OperacionNoPermitidaException.class, () -> service.generarToken(dto, "user1"));
    }

    @Test
    void generarToken_saldoInsuficiente_throws() {
        GenerarTokenRetiroSolicitudDTO dto = crearSolicitud(1L, new BigDecimal("9999.00"));
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 1L)).thenReturn(Optional.of(cuenta));
        when(tokenRepository.findByCuenta_IdCuentaAndEstado(1L, EstadoToken.ACTIVO)).thenReturn(Collections.emptyList());
        assertThrows(OperacionNoPermitidaException.class, () -> service.generarToken(dto, "user1"));
    }

    @Test
    void usarToken_exitoso() {
        TokenRetiro token = new TokenRetiro();
        token.setCodigo("123456");
        token.setMonto(new BigDecimal("100.00"));
        token.setEstado(EstadoToken.ACTIVO);
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(10));
        token.setCuenta(cuenta);

        when(tokenRepository.findByCodigo("123456")).thenReturn(Optional.of(token));

        service.usarToken("123456", new BigDecimal("100.00"));

        assertEquals(EstadoToken.USADO, token.getEstado());
        verify(movimientoRepository).save(any());
    }

    @Test
    void usarToken_codigoNoExiste_throws() {
        when(tokenRepository.findByCodigo("000000")).thenReturn(Optional.empty());
        assertThrows(OperacionNoPermitidaException.class, () -> service.usarToken("000000", BigDecimal.ONE));
    }

    @Test
    void usarToken_tokenYaUsado_throws() {
        TokenRetiro token = new TokenRetiro();
        token.setCodigo("123456");
        token.setEstado(EstadoToken.USADO);
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByCodigo("123456")).thenReturn(Optional.of(token));
        assertThrows(OperacionNoPermitidaException.class, () -> service.usarToken("123456", BigDecimal.ONE));
    }

    @Test
    void usarToken_expirado_throws() {
        TokenRetiro token = new TokenRetiro();
        token.setCodigo("123456");
        token.setEstado(EstadoToken.ACTIVO);
        token.setFechaExpiracion(LocalDateTime.now().minusMinutes(5));

        when(tokenRepository.findByCodigo("123456")).thenReturn(Optional.of(token));
        assertThrows(OperacionNoPermitidaException.class, () -> service.usarToken("123456", BigDecimal.ONE));
    }

    @Test
    void usarToken_montoNoCoincide_throws() {
        TokenRetiro token = new TokenRetiro();
        token.setCodigo("123456");
        token.setMonto(new BigDecimal("100.00"));
        token.setEstado(EstadoToken.ACTIVO);
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByCodigo("123456")).thenReturn(Optional.of(token));
        assertThrows(OperacionNoPermitidaException.class, () -> service.usarToken("123456", new BigDecimal("50.00")));
    }

    @Test
    void expirarTokens_devuelveSaldo() {
        TokenRetiro token = new TokenRetiro();
        token.setEstado(EstadoToken.ACTIVO);
        token.setFechaExpiracion(LocalDateTime.now().minusMinutes(5));
        token.setMonto(new BigDecimal("100.00"));
        token.setCuenta(cuenta);

        when(tokenRepository.findAll()).thenReturn(java.util.List.of(token));

        service.expirarTokens();

        assertEquals(EstadoToken.EXPIRADO, token.getEstado());
        assertEquals(new BigDecimal("600.00"), cuenta.getSaldo());
    }

    @Test
    void consultarEstado_retornaEstado() {
        TokenRetiro token = new TokenRetiro();
        token.setCodigo("123456");
        token.setEstado(EstadoToken.ACTIVO);
        token.setFechaExpiracion(LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByCodigo("123456")).thenReturn(Optional.of(token));

        TokenRetiroEstadoDTO dto = service.consultarEstado("123456");

        assertNotNull(dto);
        assertEquals("ACTIVO", dto.getEstado());
    }

    @Test
    void consultarEstado_tokenNoExiste_throws() {
        when(tokenRepository.findByCodigo("000000")).thenReturn(Optional.empty());
        assertThrows(OperacionNoPermitidaException.class, () -> service.consultarEstado("000000"));
    }
}
