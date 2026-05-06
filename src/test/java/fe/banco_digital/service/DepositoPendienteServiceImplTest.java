package fe.banco_digital.service;

import fe.banco_digital.dto.DepositoPendienteRespuestaDTO;
import fe.banco_digital.dto.RegistrarDepositoPendienteDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.DepositoPendienteRepository;
import fe.banco_digital.repository.UsuarioRepository;
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
class DepositoPendienteServiceImplTest {

    @Mock DepositoPendienteRepository depositoPendienteRepository;
    @Mock CuentaRepository cuentaRepository;
    @Mock UsuarioRepository usuarioRepository;

    @InjectMocks DepositoPendienteServiceImpl service;

    Usuario usuario;
    Cliente cliente;
    Cuenta cuenta;
    RegistrarDepositoPendienteDTO solicitud;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1L);

        usuario = new Usuario();
        usuario.setUsername("user");
        usuario.setCliente(cliente);

        cuenta = new Cuenta();
        cuenta.setNumeroCuenta("00010001");
        cuenta.setCliente(cliente);

        solicitud = new RegistrarDepositoPendienteDTO();
        solicitud.setNumeroCuenta("00010001");
        solicitud.setReferenciaGateway("REF-001");
        solicitud.setMonto(new BigDecimal("200000"));
    }

    @Test
    void registrar_exitoso_guardaDepositoPendiente() {
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByNumeroCuenta("00010001")).thenReturn(Optional.of(cuenta));
        when(depositoPendienteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DepositoPendienteRespuestaDTO resp = service.registrar(solicitud, "user");

        assertNotNull(resp);
        assertEquals("REF-001", resp.getReferenciaGateway());
        assertEquals("PENDIENTE", resp.getEstado());
        verify(depositoPendienteRepository).save(any());
    }

    @Test
    void registrar_usuarioNoEncontrado_throws() {
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.empty());
        assertThrows(AccesoNoAutorizadoException.class, () -> service.registrar(solicitud, "user"));
    }

    @Test
    void registrar_cuentaNoEncontrada_throws() {
        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByNumeroCuenta("00010001")).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class, () -> service.registrar(solicitud, "user"));
    }

    @Test
    void registrar_cuentaNoPertenece_throws() {
        Cliente otro = new Cliente();
        otro.setIdCliente(99L);
        cuenta.setCliente(otro);

        when(usuarioRepository.findByUsername("user")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByNumeroCuenta("00010001")).thenReturn(Optional.of(cuenta));

        assertThrows(AccesoNoAutorizadoException.class, () -> service.registrar(solicitud, "user"));
    }

    @Test
    void consultar_exitoso_retornaDTO() {
        DepositoPendiente pendiente = buildPendiente(EstadoDepositoPendiente.PENDIENTE, LocalDateTime.now().plusMinutes(10));
        when(depositoPendienteRepository.findByReferenciaGateway("REF-001")).thenReturn(Optional.of(pendiente));

        DepositoPendienteRespuestaDTO resp = service.consultar("REF-001", "user");

        assertEquals("PENDIENTE", resp.getEstado());
        assertTrue(resp.getSegundosRestantes() > 0);
    }

    @Test
    void consultar_pendienteExpirado_actualizaEstadoAExpirado() {
        DepositoPendiente pendiente = buildPendiente(EstadoDepositoPendiente.PENDIENTE, LocalDateTime.now().minusMinutes(1));
        when(depositoPendienteRepository.findByReferenciaGateway("REF-001")).thenReturn(Optional.of(pendiente));
        when(depositoPendienteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DepositoPendienteRespuestaDTO resp = service.consultar("REF-001", "user");

        assertEquals("EXPIRADO", resp.getEstado());
        verify(depositoPendienteRepository).save(pendiente);
    }

    @Test
    void consultar_referenciaNoExiste_throws() {
        when(depositoPendienteRepository.findByReferenciaGateway("NOPE")).thenReturn(Optional.empty());
        assertThrows(CuentaNoEncontradaException.class, () -> service.consultar("NOPE", "user"));
    }

    private DepositoPendiente buildPendiente(EstadoDepositoPendiente estado, LocalDateTime expiracion) {
        DepositoPendiente p = new DepositoPendiente();
        p.setReferenciaGateway("REF-001");
        p.setNumeroCuenta("00010001");
        p.setMonto(new BigDecimal("200000"));
        p.setEstado(estado);
        p.setFechaCreacion(LocalDateTime.now().minusMinutes(5));
        p.setFechaExpiracion(expiracion);
        return p;
    }
}
