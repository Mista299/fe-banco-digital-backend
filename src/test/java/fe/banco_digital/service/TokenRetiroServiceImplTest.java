package fe.banco_digital.service;

import fe.banco_digital.dto.GenerarTokenRetiroSolicitudDTO;
import fe.banco_digital.dto.TokenRetiroEstadoDTO;
import fe.banco_digital.dto.TokenRetiroRespuestaDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.RetiroRechazadoException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.TokenRetiroRepository;
import fe.banco_digital.repository.TransaccionRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("java:S100")
@ExtendWith(MockitoExtension.class)
class TokenRetiroServiceImplTest {

    private static final String USERNAME        = "user";
    private static final String CODIGO_VALIDO   = "123456";
    private static final String CODIGO_INVALIDO = "000000";
    private static final String FIELD_MONTO     = "monto";
    private static final String FIELD_ID_CUENTA = "idCuenta";
    private static final BigDecimal MONTO_NORMAL    = new BigDecimal("100000");
    private static final BigDecimal MONTO_RESERVADO = new BigDecimal("50000");
    private static final BigDecimal MONTO_EXCESIVO  = new BigDecimal("999999");

    @Mock CuentaRepository cuentaRepository;
    @Mock TokenRetiroRepository tokenRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock TransaccionRepository transaccionRepository;

    @InjectMocks TokenRetiroServiceImpl service;

    Usuario usuario;
    Cliente cliente;
    Cuenta cuenta;
    GenerarTokenRetiroSolicitudDTO solicitud;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1L);

        usuario = new Usuario();
        usuario.setUsername(USERNAME);
        usuario.setCliente(cliente);

        cuenta = new Cuenta();
        cuenta.setIdCuenta(10L);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldo(new BigDecimal("500000"));
        cuenta.setSaldoDisponible(new BigDecimal("500000"));
        cuenta.setSaldoReservado(BigDecimal.ZERO);
        cuenta.setCliente(cliente);

        solicitud = new GenerarTokenRetiroSolicitudDTO();
        ReflectionTestUtils.setField(solicitud, FIELD_ID_CUENTA, 10L);
        ReflectionTestUtils.setField(solicitud, FIELD_MONTO, MONTO_NORMAL);
    }

    @Test
    void generarToken_exitoso_reservaSaldoYRetornaToken() {
        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(10L, 1L)).thenReturn(Optional.of(cuenta));
        when(tokenRepository.save(any())).thenAnswer(inv -> {
            TokenRetiro t = inv.getArgument(0);
            t.setFechaExpiracion(LocalDateTime.now().plusMinutes(30));
            return t;
        });

        TokenRetiroRespuestaDTO resp = service.generarToken(solicitud, USERNAME);

        assertNotNull(resp.getCodigo());
        assertEquals(6, resp.getCodigo().length());
        verify(cuentaRepository).save(cuenta);
    }

    @Test
    void generarToken_cuentaNoActiva_throws() {
        cuenta.setEstado(EstadoCuenta.BLOQUEADA);
        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(10L, 1L)).thenReturn(Optional.of(cuenta));

        assertThrows(RetiroRechazadoException.class, () -> service.generarToken(solicitud, USERNAME));
    }

    @Test
    void generarToken_montoNegativo_throws() {
        ReflectionTestUtils.setField(solicitud, FIELD_MONTO, new BigDecimal("-1"));
        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(10L, 1L)).thenReturn(Optional.of(cuenta));

        assertThrows(RetiroRechazadoException.class, () -> service.generarToken(solicitud, USERNAME));
    }

    @Test
    void generarToken_saldoInsuficiente_throws() {
        ReflectionTestUtils.setField(solicitud, FIELD_MONTO, MONTO_EXCESIVO);
        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(10L, 1L)).thenReturn(Optional.of(cuenta));

        assertThrows(RetiroRechazadoException.class, () -> service.generarToken(solicitud, USERNAME));
    }

    @Test
    void generarToken_usuarioNoEncontrado_throws() {
        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        assertThrows(AutenticacionFallidaException.class, () -> service.generarToken(solicitud, USERNAME));
    }

    @Test
    void generarToken_cuentaNoPertenece_throws() {
        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(AccesoNoAutorizadoException.class, () -> service.generarToken(solicitud, USERNAME));
    }

    @Test
    void usarToken_exitoso_liberaReservaYRegistraTransaccion() {
        cuenta.setSaldoReservado(MONTO_NORMAL);
        TokenRetiro token = buildToken(EstadoToken.ACTIVO, LocalDateTime.now().plusMinutes(25), MONTO_NORMAL);
        when(tokenRepository.findByCodigo(CODIGO_VALIDO)).thenReturn(Optional.of(token));

        service.usarToken(CODIGO_VALIDO, MONTO_NORMAL);

        assertEquals(EstadoToken.USADO, token.getEstado());
        verify(cuentaRepository).save(cuenta);
        verify(transaccionRepository).save(any());
    }

    @Test
    void usarToken_tokenYaUsado_throws() {
        TokenRetiro token = buildToken(EstadoToken.USADO, LocalDateTime.now().plusMinutes(5), MONTO_NORMAL);
        when(tokenRepository.findByCodigo(CODIGO_VALIDO)).thenReturn(Optional.of(token));

        assertThrows(RetiroRechazadoException.class, () -> service.usarToken(CODIGO_VALIDO, MONTO_NORMAL));
    }

    @Test
    void usarToken_tokenExpirado_throws() {
        TokenRetiro token = buildToken(EstadoToken.ACTIVO, LocalDateTime.now().minusMinutes(1), MONTO_NORMAL);
        when(tokenRepository.findByCodigo(CODIGO_VALIDO)).thenReturn(Optional.of(token));

        assertThrows(RetiroRechazadoException.class, () -> service.usarToken(CODIGO_VALIDO, MONTO_NORMAL));
    }

    @Test
    void usarToken_montoIncorrecto_throws() {
        TokenRetiro token = buildToken(EstadoToken.ACTIVO, LocalDateTime.now().plusMinutes(5), MONTO_NORMAL);
        when(tokenRepository.findByCodigo(CODIGO_VALIDO)).thenReturn(Optional.of(token));

        assertThrows(RetiroRechazadoException.class, () -> service.usarToken(CODIGO_VALIDO, MONTO_RESERVADO));
    }

    @Test
    void usarToken_noExiste_throws() {
        when(tokenRepository.findByCodigo(CODIGO_INVALIDO)).thenReturn(Optional.empty());
        assertThrows(RetiroRechazadoException.class, () -> service.usarToken(CODIGO_INVALIDO, MONTO_NORMAL));
    }

    @Test
    void expirarTokens_expiraTokensVencidos() {
        cuenta.setSaldoReservado(MONTO_RESERVADO);
        TokenRetiro vencido = buildToken(EstadoToken.ACTIVO, LocalDateTime.now().minusMinutes(1), MONTO_RESERVADO);
        TokenRetiro activo  = buildToken(EstadoToken.ACTIVO, LocalDateTime.now().plusMinutes(20), MONTO_RESERVADO);

        when(tokenRepository.findAll()).thenReturn(List.of(vencido, activo));

        service.expirarTokens();

        assertEquals(EstadoToken.EXPIRADO, vencido.getEstado());
        assertEquals(EstadoToken.ACTIVO, activo.getEstado());
        verify(tokenRepository, times(1)).save(vencido);
        verify(cuentaRepository, times(1)).save(cuenta);
    }

    @Test
    void consultarEstado_tokenActivo_retornaSegundos() {
        TokenRetiro token = buildToken(EstadoToken.ACTIVO, LocalDateTime.now().plusMinutes(10), MONTO_NORMAL);
        when(tokenRepository.findByCodigo(CODIGO_VALIDO)).thenReturn(Optional.of(token));

        TokenRetiroEstadoDTO dto = service.consultarEstado(CODIGO_VALIDO);

        assertEquals("ACTIVO", dto.getEstado());
        assertTrue(dto.getSegundosRestantes() > 0);
    }

    @Test
    void consultarEstado_tokenNoExiste_throws() {
        when(tokenRepository.findByCodigo(CODIGO_INVALIDO)).thenReturn(Optional.empty());
        assertThrows(RetiroRechazadoException.class, () -> service.consultarEstado(CODIGO_INVALIDO));
    }

    private TokenRetiro buildToken(EstadoToken estado, LocalDateTime expiracion, BigDecimal monto) {
        TokenRetiro t = new TokenRetiro();
        t.setCodigo(CODIGO_VALIDO);
        t.setEstado(estado);
        t.setFechaExpiracion(expiracion);
        t.setMonto(monto);
        t.setCuenta(cuenta);
        return t;
    }
}
