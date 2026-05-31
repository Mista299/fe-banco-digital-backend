package fe.banco_digital.service;

import fe.banco_digital.dto.DecisionAperturaRespuestaDTO;
import fe.banco_digital.dto.SolicitudPendienteDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.event.AuditoriaEvent;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.exception.OperacionNoPermitidaException;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCuentaServiceImplTest {

    @Mock CuentaRepository cuentaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks AdminCuentaServiceImpl service;

    Usuario admin;
    Cliente cliente;
    Cuenta cuenta;

    @BeforeEach
    void setUp() {
        admin = new Usuario();
        admin.setIdUsuario(99L);
        admin.setUsername("admin");

        cliente = new Cliente();
        cliente.setIdCliente(1L);

        cuenta = new Cuenta();
        cuenta.setIdCuenta(10L);
        cuenta.setNumeroCuenta("11112222");
        cuenta.setEstado(EstadoCuenta.PENDIENTE_APROBACION);
        cuenta.setTipo(TipoCuenta.AHORROS);
        cuenta.setSaldo(BigDecimal.ZERO);
        cuenta.setCliente(cliente);
    }

    @Test
    void listarPendientes_retornaLista() {
        when(cuentaRepository.findByEstadoConCliente(EstadoCuenta.PENDIENTE_APROBACION))
                .thenReturn(List.of(cuenta));

        List<SolicitudPendienteDTO> resultado = service.listarPendientes();

        assertEquals(1, resultado.size());
    }

    @Test
    void listarPendientes_sinSolicitudes_retornaVacio() {
        when(cuentaRepository.findByEstadoConCliente(EstadoCuenta.PENDIENTE_APROBACION))
                .thenReturn(Collections.emptyList());

        List<SolicitudPendienteDTO> resultado = service.listarPendientes();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void aprobarApertura_exitoso() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.countByClienteIdClienteAndEstadoIn(eq(1L), any())).thenReturn(1L);

        DecisionAperturaRespuestaDTO resultado = service.aprobarApertura(10L, "admin");

        assertEquals("ACTIVA", resultado.getEstado());
        verify(cuentaRepository).save(cuenta);
        verify(eventPublisher).publishEvent(any(AuditoriaEvent.class));
    }

    @Test
    void aprobarApertura_adminNoEncontrado_throws() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.empty());

        assertThrows(AutenticacionFallidaException.class, () -> service.aprobarApertura(10L, "admin"));
    }

    @Test
    void aprobarApertura_cuentaNoEncontrada_throws() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(cuentaRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class, () -> service.aprobarApertura(10L, "admin"));
    }

    @Test
    void aprobarApertura_cuentaNoEsPendiente_throws() {
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuenta));

        assertThrows(OperacionNoPermitidaException.class, () -> service.aprobarApertura(10L, "admin"));
    }

    @Test
    void aprobarApertura_limiteAlcanzado_throws() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.countByClienteIdClienteAndEstadoIn(eq(1L), any())).thenReturn(4L);

        assertThrows(OperacionNoPermitidaException.class, () -> service.aprobarApertura(10L, "admin"));
    }

    @Test
    void aprobarApertura_corrienteSinAhorros_throws() {
        cuenta.setTipo(TipoCuenta.CORRIENTE);
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.countByClienteIdClienteAndEstadoIn(eq(1L), any())).thenReturn(1L);
        when(cuentaRepository.countByClienteIdClienteAndTipoAndEstadoIn(eq(1L), eq(TipoCuenta.AHORROS), any()))
                .thenReturn(0L);

        assertThrows(OperacionNoPermitidaException.class, () -> service.aprobarApertura(10L, "admin"));
    }

    @Test
    void rechazarApertura_exitoso() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuenta));

        DecisionAperturaRespuestaDTO resultado = service.rechazarApertura(10L, "admin");

        assertEquals("INACTIVA", resultado.getEstado());
        verify(cuentaRepository).save(cuenta);
    }

    @Test
    void rechazarApertura_cuentaNoEsPendiente_throws() {
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(cuentaRepository.findById(10L)).thenReturn(Optional.of(cuenta));

        assertThrows(OperacionNoPermitidaException.class, () -> service.rechazarApertura(10L, "admin"));
    }
}
