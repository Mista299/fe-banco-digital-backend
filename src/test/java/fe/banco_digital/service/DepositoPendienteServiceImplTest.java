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
        cuenta.setCliente(cliente);
    }

    @Test
    void registrar_exitoso() {
        RegistrarDepositoPendienteDTO dto = new RegistrarDepositoPendienteDTO();
        dto.setReferenciaGateway("REF001");
        dto.setNumeroCuenta("11112222");
        dto.setMonto(new BigDecimal("200.00"));

        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByNumeroCuenta("11112222")).thenReturn(Optional.of(cuenta));
        when(depositoPendienteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DepositoPendienteRespuestaDTO resultado = service.registrar(dto, "user1");

        assertNotNull(resultado);
        assertEquals("REF001", resultado.getReferenciaGateway());
        verify(depositoPendienteRepository).save(any());
    }

    @Test
    void registrar_usuarioNoEncontrado_throws() {
        RegistrarDepositoPendienteDTO dto = new RegistrarDepositoPendienteDTO();
        dto.setNumeroCuenta("11112222");
        dto.setMonto(new BigDecimal("200.00"));

        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.empty());

        assertThrows(AccesoNoAutorizadoException.class, () -> service.registrar(dto, "user1"));
    }

    @Test
    void registrar_cuentaNoEncontrada_throws() {
        RegistrarDepositoPendienteDTO dto = new RegistrarDepositoPendienteDTO();
        dto.setNumeroCuenta("00000000");
        dto.setMonto(new BigDecimal("200.00"));

        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByNumeroCuenta("00000000")).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class, () -> service.registrar(dto, "user1"));
    }

    @Test
    void registrar_cuentaNoPerteneceAlUsuario_throws() {
        Cliente otroCliente = new Cliente();
        otroCliente.setIdCliente(99L);
        cuenta.setCliente(otroCliente);

        RegistrarDepositoPendienteDTO dto = new RegistrarDepositoPendienteDTO();
        dto.setNumeroCuenta("11112222");
        dto.setMonto(new BigDecimal("200.00"));

        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByNumeroCuenta("11112222")).thenReturn(Optional.of(cuenta));

        assertThrows(AccesoNoAutorizadoException.class, () -> service.registrar(dto, "user1"));
    }

    @Test
    void consultar_estadoPendienteActivo() {
        DepositoPendiente pendiente = new DepositoPendiente();
        pendiente.setReferenciaGateway("REF001");
        pendiente.setEstado(EstadoDepositoPendiente.PENDIENTE);
        pendiente.setFechaExpiracion(LocalDateTime.now().plusMinutes(10));
        pendiente.setMonto(new BigDecimal("200.00"));
        pendiente.setCuenta(cuenta);

        when(depositoPendienteRepository.findByReferenciaGateway("REF001")).thenReturn(Optional.of(pendiente));

        DepositoPendienteRespuestaDTO resultado = service.consultar("REF001", "user1");

        assertNotNull(resultado);
        assertEquals("PENDIENTE", resultado.getEstado());
    }

    @Test
    void consultar_pendienteExpirado_actualizaEstado() {
        DepositoPendiente pendiente = new DepositoPendiente();
        pendiente.setReferenciaGateway("REF001");
        pendiente.setEstado(EstadoDepositoPendiente.PENDIENTE);
        pendiente.setFechaExpiracion(LocalDateTime.now().minusMinutes(5));
        pendiente.setMonto(new BigDecimal("200.00"));

        when(depositoPendienteRepository.findByReferenciaGateway("REF001")).thenReturn(Optional.of(pendiente));

        DepositoPendienteRespuestaDTO resultado = service.consultar("REF001", "user1");

        assertEquals("EXPIRADO", resultado.getEstado());
        verify(depositoPendienteRepository).save(pendiente);
    }

    @Test
    void consultar_referenciaNoEncontrada_throws() {
        when(depositoPendienteRepository.findByReferenciaGateway("REF999")).thenReturn(Optional.empty());

        assertThrows(CuentaNoEncontradaException.class, () -> service.consultar("REF999", "user1"));
    }
}
