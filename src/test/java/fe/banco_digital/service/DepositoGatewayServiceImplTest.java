package fe.banco_digital.service;

import fe.banco_digital.dto.ComprobanteDepositoDTO;
import fe.banco_digital.dto.NotificacionDepositoDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.exception.OperacionNoPermitidaException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.DepositoPendienteRepository;
import fe.banco_digital.repository.MovimientoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositoGatewayServiceImplTest {

    @Mock CuentaRepository cuentaRepository;
    @Mock MovimientoRepository movimientoRepository;
    @Mock DepositoPendienteRepository depositoPendienteRepository;
    @Mock RegistroFalloService registroFalloService;

    @InjectMocks DepositoGatewayServiceImpl service;

    Cuenta cuenta;
    DepositoPendiente pendiente;
    NotificacionDepositoDTO notificacion;

    @BeforeEach
    void setUp() {
        cuenta = new Cuenta();
        cuenta.setIdCuenta(1L);
        cuenta.setNumeroCuenta("11112222");
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldo(new BigDecimal("500.00"));

        pendiente = new DepositoPendiente();
        pendiente.setReferenciaGateway("REF001");
        pendiente.setCuenta(cuenta);
        pendiente.setMonto(new BigDecimal("200.00"));
        pendiente.setEstado(EstadoDepositoPendiente.PENDIENTE);
        pendiente.setFechaExpiracion(LocalDateTime.now().plusMinutes(10));

        notificacion = new NotificacionDepositoDTO();
        notificacion.setReferenciaGateway("REF001");
        notificacion.setNumeroCuenta("11112222");
        notificacion.setMonto(new BigDecimal("200.00"));
    }

    @Test
    void procesarNotificacion_exitoso() {
        when(depositoPendienteRepository.findByReferenciaGateway("REF001")).thenReturn(Optional.of(pendiente));
        when(cuentaRepository.findByNumeroCuenta("11112222")).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.save(any())).thenAnswer(inv -> {
            Movimiento m = inv.getArgument(0);
            m.setIdMovimiento(1L);
            return m;
        });

        ComprobanteDepositoDTO resultado = service.procesarNotificacion(notificacion);

        assertNotNull(resultado);
        assertEquals(new BigDecimal("700.00"), cuenta.getSaldo());
        assertEquals(EstadoDepositoPendiente.COMPLETADO, pendiente.getEstado());
    }

    @Test
    void procesarNotificacion_referenciaNoEncontrada_throws() {
        when(depositoPendienteRepository.findByReferenciaGateway("REF001")).thenReturn(Optional.empty());

        assertThrows(OperacionNoPermitidaException.class, () -> service.procesarNotificacion(notificacion));
    }

    @Test
    void procesarNotificacion_yaCompletado_throws() {
        pendiente.setEstado(EstadoDepositoPendiente.COMPLETADO);
        when(depositoPendienteRepository.findByReferenciaGateway("REF001")).thenReturn(Optional.of(pendiente));

        assertThrows(OperacionNoPermitidaException.class, () -> service.procesarNotificacion(notificacion));
    }

    @Test
    void procesarNotificacion_expirado_throws() {
        pendiente.setEstado(EstadoDepositoPendiente.EXPIRADO);
        when(depositoPendienteRepository.findByReferenciaGateway("REF001")).thenReturn(Optional.of(pendiente));

        assertThrows(OperacionNoPermitidaException.class, () -> service.procesarNotificacion(notificacion));
    }

    @Test
    void procesarNotificacion_fechaExpirada_throws() {
        pendiente.setFechaExpiracion(LocalDateTime.now().minusMinutes(5));
        when(depositoPendienteRepository.findByReferenciaGateway("REF001")).thenReturn(Optional.of(pendiente));

        assertThrows(OperacionNoPermitidaException.class, () -> service.procesarNotificacion(notificacion));
    }

    @Test
    void procesarNotificacion_montoNoCoincide_throws() {
        notificacion.setMonto(new BigDecimal("999.00"));
        when(depositoPendienteRepository.findByReferenciaGateway("REF001")).thenReturn(Optional.of(pendiente));

        assertThrows(OperacionNoPermitidaException.class, () -> service.procesarNotificacion(notificacion));
    }

    @Test
    void procesarNotificacion_cuentaNoCoincide_throws() {
        notificacion.setNumeroCuenta("99999999");
        when(depositoPendienteRepository.findByReferenciaGateway("REF001")).thenReturn(Optional.of(pendiente));

        assertThrows(OperacionNoPermitidaException.class, () -> service.procesarNotificacion(notificacion));
    }

    @Test
    void procesarNotificacion_cuentaBloqueada_throws() {
        cuenta.setEstado(EstadoCuenta.BLOQUEADA);
        when(depositoPendienteRepository.findByReferenciaGateway("REF001")).thenReturn(Optional.of(pendiente));
        when(cuentaRepository.findByNumeroCuenta("11112222")).thenReturn(Optional.of(cuenta));

        assertThrows(OperacionNoPermitidaException.class, () -> service.procesarNotificacion(notificacion));
        verify(registroFalloService).registrarFalloMovimiento(any(), any(), any());
    }

    @Test
    void procesarNotificacion_cuentaInactiva_throws() {
        cuenta.setEstado(EstadoCuenta.INACTIVA);
        when(depositoPendienteRepository.findByReferenciaGateway("REF001")).thenReturn(Optional.of(pendiente));
        when(cuentaRepository.findByNumeroCuenta("11112222")).thenReturn(Optional.of(cuenta));

        assertThrows(OperacionNoPermitidaException.class, () -> service.procesarNotificacion(notificacion));
    }
}
