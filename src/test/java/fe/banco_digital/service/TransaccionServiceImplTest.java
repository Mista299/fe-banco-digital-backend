package fe.banco_digital.service;

import fe.banco_digital.dto.DepositoSolicitudDTO;
import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.dto.RetiroSolicitudDTO;
import fe.banco_digital.dto.TransaccionRespuestaDTO;
import fe.banco_digital.dto.TransferenciaSolicitudDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.EstadoTransaccion;
import fe.banco_digital.entity.TipoCuenta;
import fe.banco_digital.entity.TipoTransaccion;
import fe.banco_digital.entity.Transaccion;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.CuentaBloqueadaException;
import fe.banco_digital.exception.CuentaYaCerradaException;
import fe.banco_digital.exception.SaldoInsuficienteException;
import fe.banco_digital.mapper.TransaccionMapper;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.TransaccionRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceImplTest {

    @Mock TransaccionRepository transaccionRepository;
    @Mock TransaccionMapper transaccionMapper;
    @Mock UsuarioRepository usuarioRepository;
    @Mock CuentaRepository cuentaRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock RegistroFalloService registroFalloService;

    @InjectMocks TransaccionServiceImpl service;

    Cliente cliente;
    Usuario usuario;
    Cuenta cuenta;
    Transaccion transaccionGuardada;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(3L);

        usuario = new Usuario();
        usuario.setIdUsuario(8L);
        usuario.setUsername("clienteTest");
        usuario.setCliente(cliente);

        cuenta = new Cuenta();
        cuenta.setIdCuenta(1L);
        cuenta.setNumeroCuenta("11223344");
        cuenta.setTipo(TipoCuenta.AHORROS);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldo(new BigDecimal("1000.00"));
        cuenta.setCliente(cliente);

        transaccionGuardada = new Transaccion();
        transaccionGuardada.setIdTransaccion(99L);
        transaccionGuardada.setTipo(TipoTransaccion.DEPOSITO);
        transaccionGuardada.setMonto(new BigDecimal("200.00"));
        transaccionGuardada.setEstado(EstadoTransaccion.EXITOSA);
        transaccionGuardada.setFecha(LocalDateTime.now());
    }

    private void mockResolverCuentaConLock() {
        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 3L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.findByIdCuentaConLock(1L)).thenReturn(Optional.of(cuenta));
    }

    // ── depositar ────────────────────────────────────────────────────────────

    @Test
    void depositar_exitoso_actualizaSaldoYPublicaEvento() {
        mockResolverCuentaConLock();
        when(transaccionRepository.save(any())).thenReturn(transaccionGuardada);

        DepositoSolicitudDTO dto = new DepositoSolicitudDTO();
        dto.setIdCuenta(1L);
        dto.setMonto(new BigDecimal("200.00"));

        TransaccionRespuestaDTO result = service.depositar(dto, "clienteTest");

        assertEquals(new BigDecimal("1200.00"), cuenta.getSaldo());
        verify(cuentaRepository).save(cuenta);
        verify(eventPublisher).publishEvent(any());
        assertNotNull(result);
    }

    @Test
    void depositar_cuentaBloqueada_throws() {
        cuenta.setEstado(EstadoCuenta.BLOQUEADA);
        mockResolverCuentaConLock();

        DepositoSolicitudDTO dto = new DepositoSolicitudDTO();
        dto.setIdCuenta(1L);
        dto.setMonto(new BigDecimal("100.00"));

        assertThrows(CuentaBloqueadaException.class, () -> service.depositar(dto, "clienteTest"));
    }

    @Test
    void depositar_cuentaCerrada_throws() {
        cuenta.setEstado(EstadoCuenta.INACTIVA);
        mockResolverCuentaConLock();

        DepositoSolicitudDTO dto = new DepositoSolicitudDTO();
        dto.setIdCuenta(1L);
        dto.setMonto(new BigDecimal("100.00"));

        assertThrows(CuentaYaCerradaException.class, () -> service.depositar(dto, "clienteTest"));
    }

    @Test
    void depositar_usuarioNoEncontrado_throws() {
        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.empty());

        DepositoSolicitudDTO dto = new DepositoSolicitudDTO();
        dto.setIdCuenta(1L);
        dto.setMonto(new BigDecimal("100.00"));

        assertThrows(AutenticacionFallidaException.class, () -> service.depositar(dto, "clienteTest"));
    }

    // ── retirar ──────────────────────────────────────────────────────────────

    @Test
    void retirar_exitoso_descontaSaldoYPublicaEvento() {
        mockResolverCuentaConLock();
        transaccionGuardada.setTipo(TipoTransaccion.RETIRO);
        when(transaccionRepository.save(any())).thenReturn(transaccionGuardada);

        RetiroSolicitudDTO dto = new RetiroSolicitudDTO();
        dto.setIdCuenta(1L);
        dto.setMonto(new BigDecimal("300.00"));

        TransaccionRespuestaDTO result = service.retirar(dto, "clienteTest");

        assertEquals(new BigDecimal("700.00"), cuenta.getSaldo());
        verify(cuentaRepository).save(cuenta);
        verify(eventPublisher).publishEvent(any());
        assertNotNull(result);
    }

    @Test
    void retirar_saldoInsuficiente_registraFalloYThrows() {
        mockResolverCuentaConLock();

        RetiroSolicitudDTO dto = new RetiroSolicitudDTO();
        dto.setIdCuenta(1L);
        dto.setMonto(new BigDecimal("5000.00"));

        assertThrows(SaldoInsuficienteException.class, () -> service.retirar(dto, "clienteTest"));
        verify(registroFalloService).registrarFallo(eq(cuenta), eq(null), eq(TipoTransaccion.RETIRO), any());
    }

    @Test
    void retirar_cuentaBloqueada_throws() {
        cuenta.setEstado(EstadoCuenta.BLOQUEADA);
        mockResolverCuentaConLock();

        RetiroSolicitudDTO dto = new RetiroSolicitudDTO();
        dto.setIdCuenta(1L);
        dto.setMonto(new BigDecimal("100.00"));

        assertThrows(CuentaBloqueadaException.class, () -> service.retirar(dto, "clienteTest"));
    }

    // ── transferir ───────────────────────────────────────────────────────────

    @Test
    void transferir_exitoso_muevesSaldoEntreAmbas() {
        Cuenta destino = new Cuenta();
        destino.setIdCuenta(2L);
        destino.setNumeroCuenta("55667788");
        destino.setEstado(EstadoCuenta.ACTIVA);
        destino.setSaldo(new BigDecimal("500.00"));

        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByNumeroCuenta("55667788")).thenReturn(Optional.of(destino));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 3L)).thenReturn(Optional.of(cuenta));
        // idOrigen=1 < idDestino=2 → primerLock=origen, segundoLock=destino
        when(cuentaRepository.findByIdCuentaConLock(1L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.findByIdCuentaConLock(2L)).thenReturn(Optional.of(destino));
        transaccionGuardada.setTipo(TipoTransaccion.TRANSFERENCIA);
        when(transaccionRepository.save(any())).thenReturn(transaccionGuardada);

        TransferenciaSolicitudDTO dto = new TransferenciaSolicitudDTO();
        dto.setIdCuentaOrigen(1L);
        dto.setNumeroCuentaDestino("55667788");
        dto.setMonto(new BigDecimal("200.00"));

        service.transferir(dto, "clienteTest");

        assertEquals(new BigDecimal("800.00"), cuenta.getSaldo());
        assertEquals(new BigDecimal("700.00"), destino.getSaldo());
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void transferir_saldoInsuficiente_throws() {
        Cuenta destino = new Cuenta();
        destino.setIdCuenta(2L);
        destino.setNumeroCuenta("55667788");
        destino.setEstado(EstadoCuenta.ACTIVA);
        destino.setSaldo(BigDecimal.ZERO);

        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByNumeroCuenta("55667788")).thenReturn(Optional.of(destino));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 3L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.findByIdCuentaConLock(1L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.findByIdCuentaConLock(2L)).thenReturn(Optional.of(destino));

        TransferenciaSolicitudDTO dto = new TransferenciaSolicitudDTO();
        dto.setIdCuentaOrigen(1L);
        dto.setNumeroCuentaDestino("55667788");
        dto.setMonto(new BigDecimal("9999.00"));

        assertThrows(SaldoInsuficienteException.class, () -> service.transferir(dto, "clienteTest"));
    }

    @Test
    void transferir_destinoBloqueado_throws() {
        Cuenta destino = new Cuenta();
        destino.setIdCuenta(2L);
        destino.setNumeroCuenta("55667788");
        destino.setEstado(EstadoCuenta.BLOQUEADA);

        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByNumeroCuenta("55667788")).thenReturn(Optional.of(destino));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 3L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.findByIdCuentaConLock(1L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.findByIdCuentaConLock(2L)).thenReturn(Optional.of(destino));

        TransferenciaSolicitudDTO dto = new TransferenciaSolicitudDTO();
        dto.setIdCuentaOrigen(1L);
        dto.setNumeroCuentaDestino("55667788");
        dto.setMonto(new BigDecimal("100.00"));

        assertThrows(CuentaBloqueadaException.class, () -> service.transferir(dto, "clienteTest"));
    }

    @Test
    void transferir_sinAccesoACuentaOrigen_throws() {
        Cuenta destino = new Cuenta();
        destino.setIdCuenta(2L);
        destino.setNumeroCuenta("55667788");

        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByNumeroCuenta("55667788")).thenReturn(Optional.of(destino));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 3L)).thenReturn(Optional.empty());

        TransferenciaSolicitudDTO dto = new TransferenciaSolicitudDTO();
        dto.setIdCuentaOrigen(1L);
        dto.setNumeroCuentaDestino("55667788");
        dto.setMonto(new BigDecimal("100.00"));

        assertThrows(AccesoNoAutorizadoException.class, () -> service.transferir(dto, "clienteTest"));
    }

    // ── obtenerMovimientos ───────────────────────────────────────────────────

    @Test
    void obtenerMovimientos_exitoso_retornaLista() {
        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 3L)).thenReturn(Optional.of(cuenta));
        when(transaccionRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transaccionMapper.aListaDTO(any(), eq(1L))).thenReturn(Collections.emptyList());

        List<MovimientoDTO> result = service.obtenerMovimientos(1L, "clienteTest");

        assertNotNull(result);
        verify(transaccionRepository).findByCuentaIdOrderByFechaDesc(1L);
    }

    @Test
    void obtenerMovimientos_sinAcceso_throws() {
        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 3L)).thenReturn(Optional.empty());

        assertThrows(AccesoNoAutorizadoException.class, () -> service.obtenerMovimientos(1L, "clienteTest"));
    }
}
