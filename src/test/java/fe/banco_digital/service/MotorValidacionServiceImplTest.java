package fe.banco_digital.service;

import fe.banco_digital.dto.ValidacionTransaccionResponseDTO;
import fe.banco_digital.dto.ValidacionTransaccionSolicitudDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MotorValidacionServiceImplTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock CuentaRepository cuentaRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks MotorValidacionServiceImpl service;

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
        cuenta.setNumeroCuenta("11112222");
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldo(new BigDecimal("1000.00"));
    }

    @Test
    void validar_cuentaActivaConSaldo_autorizada() {
        ValidacionTransaccionSolicitudDTO dto = new ValidacionTransaccionSolicitudDTO();
        dto.setIdCuentaOrigen(1L);
        dto.setMonto(new BigDecimal("100.00"));

        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 1L)).thenReturn(Optional.of(cuenta));

        ValidacionTransaccionResponseDTO resultado = service.validar(dto, "user1");

        assertTrue(resultado.isAutorizada());
        assertEquals("AUTORIZADA", resultado.getCodigo());
    }

    @Test
    void validar_usuarioNoEncontrado_throws() {
        ValidacionTransaccionSolicitudDTO dto = new ValidacionTransaccionSolicitudDTO();
        dto.setIdCuentaOrigen(1L);
        dto.setMonto(new BigDecimal("100.00"));

        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.empty());

        assertThrows(AutenticacionFallidaException.class, () -> service.validar(dto, "user1"));
    }

    @Test
    void validar_cuentaNoPertenece_throws() {
        ValidacionTransaccionSolicitudDTO dto = new ValidacionTransaccionSolicitudDTO();
        dto.setIdCuentaOrigen(1L);
        dto.setMonto(new BigDecimal("100.00"));

        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(AccesoNoAutorizadoException.class, () -> service.validar(dto, "user1"));
    }

    @Test
    void validarCuentaParaDebito_cuentaNull_throws() {
        assertThrows(CuentaNoEncontradaException.class,
                () -> service.validarCuentaParaDebito(null, BigDecimal.ONE));
    }

    @Test
    void validarCuentaParaDebito_cuentaBloqueada_rechazada() {
        cuenta.setEstado(EstadoCuenta.BLOQUEADA);

        ValidacionTransaccionResponseDTO resultado = service.validarCuentaParaDebito(cuenta, new BigDecimal("100.00"));

        assertFalse(resultado.isAutorizada());
        assertEquals("CUENTA_BLOQUEADA", resultado.getCodigo());
    }

    @Test
    void validarCuentaParaDebito_cuentaInactiva_rechazada() {
        cuenta.setEstado(EstadoCuenta.INACTIVA);

        ValidacionTransaccionResponseDTO resultado = service.validarCuentaParaDebito(cuenta, new BigDecimal("100.00"));

        assertFalse(resultado.isAutorizada());
        assertEquals("CUENTA_INACTIVA", resultado.getCodigo());
    }

    @Test
    void validarCuentaParaDebito_saldoInsuficiente_rechazada() {
        ValidacionTransaccionResponseDTO resultado = service.validarCuentaParaDebito(cuenta, new BigDecimal("9999.00"));

        assertFalse(resultado.isAutorizada());
        assertEquals("SALDO_INSUFICIENTE", resultado.getCodigo());
    }

    @Test
    void validarCuentaParaDebito_saldoNull_rechazada() {
        cuenta.setSaldo(null);

        ValidacionTransaccionResponseDTO resultado = service.validarCuentaParaDebito(cuenta, new BigDecimal("100.00"));

        assertFalse(resultado.isAutorizada());
        assertEquals("SALDO_INSUFICIENTE", resultado.getCodigo());
    }

    @Test
    void validar_cuentaBloqueada_publicaAuditoria() {
        cuenta.setEstado(EstadoCuenta.BLOQUEADA);
        ValidacionTransaccionSolicitudDTO dto = new ValidacionTransaccionSolicitudDTO();
        dto.setIdCuentaOrigen(1L);
        dto.setMonto(new BigDecimal("100.00"));

        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 1L)).thenReturn(Optional.of(cuenta));

        ValidacionTransaccionResponseDTO resultado = service.validar(dto, "user1");

        assertFalse(resultado.isAutorizada());
        verify(eventPublisher).publishEvent(any());
    }
}
