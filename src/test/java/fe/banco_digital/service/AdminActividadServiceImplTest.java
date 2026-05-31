package fe.banco_digital.service;

import fe.banco_digital.dto.ActividadClienteResponseDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.ClienteNoEncontradoException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.mapper.TransaccionMapper;
import fe.banco_digital.repository.*;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminActividadServiceImplTest {

    @Mock ClienteRepository clienteRepository;
    @Mock CuentaRepository cuentaRepository;
    @Mock MovimientoRepository movimientoRepository;
    @Mock TransferenciaRepository transferenciaRepository;
    @Mock TransferenciaExternaRepository transferenciaExternaRepository;
    @Mock TransferenciaInternacionalRepository transferenciaInternacionalRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock TransaccionMapper transaccionMapper;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks AdminActividadServiceImpl service;

    Cliente cliente;
    Usuario admin;
    Cuenta cuenta;
    Rol rolAdmin;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Juan Test");
        cliente.setDocumento("12345678");

        rolAdmin = new Rol();
        rolAdmin.setNombre(RolNombre.ADMIN);

        admin = new Usuario();
        admin.setIdUsuario(99L);
        admin.setUsername("admin");
        admin.setRoles(Set.of(rolAdmin));

        cuenta = new Cuenta();
        cuenta.setIdCuenta(1L);
        cuenta.setNumeroCuenta("11112222");
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldo(new BigDecimal("1000.00"));
        cuenta.setCliente(cliente);
    }

    @Test
    void consultarActividadPorDocumento_exitoso() {
        when(clienteRepository.findByDocumento("12345678")).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(cuentaRepository.findByCliente_IdClienteOrderByIdCuentaAsc(1L)).thenReturn(List.of(cuenta));
        when(movimientoRepository.findByCuenta_IdCuentaOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transferenciaRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transferenciaExternaRepository.findByCuentaOrigen_IdCuentaOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transferenciaInternacionalRepository.findByCuentaOrigen_IdCuentaOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transaccionMapper.aListaDTOUnificada(any(), any(), eq(1L), any(), any())).thenReturn(Collections.emptyList());

        ActividadClienteResponseDTO resultado = service.consultarActividadPorDocumento(
                "12345678", null, null, null, "admin");

        assertNotNull(resultado);
        assertNotNull(resultado.getCliente());
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void consultarActividadPorDocumento_clienteNoEncontrado_throws() {
        when(clienteRepository.findByDocumento("99999999")).thenReturn(Optional.empty());

        assertThrows(ClienteNoEncontradoException.class,
                () -> service.consultarActividadPorDocumento("99999999", null, null, null, "admin"));
    }

    @Test
    void consultarActividadPorDocumento_adminNoEncontrado_throws() {
        when(clienteRepository.findByDocumento("12345678")).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.empty());

        assertThrows(AutenticacionFallidaException.class,
                () -> service.consultarActividadPorDocumento("12345678", null, null, null, "admin"));
    }

    @Test
    void consultarActividadPorDocumento_sinCuentas_throws() {
        when(clienteRepository.findByDocumento("12345678")).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(cuentaRepository.findByCliente_IdClienteOrderByIdCuentaAsc(1L)).thenReturn(Collections.emptyList());

        assertThrows(CuentaNoEncontradaException.class,
                () -> service.consultarActividadPorDocumento("12345678", null, null, null, "admin"));
    }

    @Test
    void consultarActividadPorNumeroCuenta_exitoso() {
        when(cuentaRepository.findByNumeroCuenta("11112222")).thenReturn(Optional.of(cuenta));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(cuentaRepository.findByCliente_IdClienteOrderByIdCuentaAsc(1L)).thenReturn(List.of(cuenta));
        when(movimientoRepository.findByCuenta_IdCuentaOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transferenciaRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transferenciaExternaRepository.findByCuentaOrigen_IdCuentaOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transferenciaInternacionalRepository.findByCuentaOrigen_IdCuentaOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transaccionMapper.aListaDTOUnificada(any(), any(), eq(1L), any(), any())).thenReturn(Collections.emptyList());

        ActividadClienteResponseDTO resultado = service.consultarActividadPorNumeroCuenta(
                "11112222", null, null, null, "admin");

        assertNotNull(resultado);
    }

    @Test
    void consultarActividadPorNumeroCuenta_cuentaNoEncontrada_throws() {
        when(cuentaRepository.findByNumeroCuenta("99999999")).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class,
                () -> service.consultarActividadPorNumeroCuenta("99999999", null, null, null, "admin"));
    }

    @Test
    void consultarActividad_filtroTipoMovimiento_aplicaFiltro() {
        when(clienteRepository.findByDocumento("12345678")).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(cuentaRepository.findByCliente_IdClienteOrderByIdCuentaAsc(1L)).thenReturn(List.of(cuenta));
        when(movimientoRepository.findByCuenta_IdCuentaOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transferenciaRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transferenciaExternaRepository.findByCuentaOrigen_IdCuentaOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transferenciaInternacionalRepository.findByCuentaOrigen_IdCuentaOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transaccionMapper.aListaDTOUnificada(any(), any(), eq(1L), any(), any())).thenReturn(Collections.emptyList());

        ActividadClienteResponseDTO resultado = service.consultarActividadPorDocumento(
                "12345678", null, null, "DEPOSITO", "admin");

        assertNotNull(resultado);
        assertTrue(resultado.getMovimientos().isEmpty());
    }
}
