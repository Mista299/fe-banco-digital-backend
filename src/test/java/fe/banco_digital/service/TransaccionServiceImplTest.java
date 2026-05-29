package fe.banco_digital.service;

import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.dto.TransaccionRespuestaDTO;
import fe.banco_digital.dto.TransferenciaSolicitudDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.Transferencia;
import fe.banco_digital.entity.TipoCuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.CuentaBloqueadaException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.exception.SaldoInsuficienteException;
import fe.banco_digital.mapper.TransaccionMapper;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.MovimientoRepository;
import fe.banco_digital.repository.TransferenciaExternaRepository;
import fe.banco_digital.repository.TransferenciaInternacionalRepository;
import fe.banco_digital.repository.TransferenciaRepository;
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

    @Mock MovimientoRepository movimientoRepository;
    @Mock TransferenciaRepository transferenciaRepository;
    @Mock TransferenciaExternaRepository transferenciaExternaRepository;
    @Mock TransferenciaInternacionalRepository transferenciaInternacionalRepository;
    @Mock TransaccionMapper transaccionMapper;
    @Mock UsuarioRepository usuarioRepository;
    @Mock CuentaRepository cuentaRepository;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock RegistroFalloService registroFalloService;

    @InjectMocks TransaccionServiceImpl service;

    Cliente cliente;
    Usuario usuario;
    Cuenta cuenta;

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
        when(transferenciaRepository.save(any())).thenAnswer(inv -> {
            Transferencia t = inv.getArgument(0);
            t.setIdTransferencia(99L);
            t.setFecha(LocalDateTime.now());
            return t;
        });

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
        when(movimientoRepository.findByCuenta_IdCuentaOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transferenciaRepository.findByCuentaIdOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transferenciaExternaRepository.findByCuentaOrigen_IdCuentaOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transferenciaInternacionalRepository.findByCuentaOrigen_IdCuentaOrderByFechaDesc(1L)).thenReturn(Collections.emptyList());
        when(transaccionMapper.aListaDTOUnificada(any(), any(), eq(1L), any(), any())).thenReturn(Collections.emptyList());

        List<MovimientoDTO> result = service.obtenerMovimientos(1L, "clienteTest");

        assertNotNull(result);
        verify(movimientoRepository).findByCuenta_IdCuentaOrderByFechaDesc(1L);
    }

    @Test
    void obtenerMovimientos_sinAcceso_throws() {
        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 3L)).thenReturn(Optional.empty());

        assertThrows(AccesoNoAutorizadoException.class, () -> service.obtenerMovimientos(1L, "clienteTest"));
    }

    // ── transferir (casos adicionales) ───────────────────────────────────────

    @Test
    void transferir_exitoso_mueveSSaldo() {
        Cuenta destino = cuentaActivaAux(2L, "55667788");
        destino.setSaldo(new BigDecimal("500.00"));

        when(cuentaRepository.findByNumeroCuenta("55667788")).thenReturn(Optional.of(destino));
        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 3L))
                .thenReturn(Optional.of(cuenta));
        when(cuentaRepository.findByIdCuentaConLock(1L)).thenReturn(Optional.of(cuenta));
        when(cuentaRepository.findByIdCuentaConLock(2L)).thenReturn(Optional.of(destino));
        when(cuentaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(transferenciaRepository.save(any())).thenAnswer(inv -> {
            Transferencia t = inv.getArgument(0);
            t.setIdTransferencia(1L);
            t.setFecha(LocalDateTime.now());
            return t;
        });

        TransferenciaSolicitudDTO dto = transferenciaDTO(1L, "55667788", "200.00");
        TransaccionRespuestaDTO result = service.transferir(dto, "clienteTest");

        assertNotNull(result);
        assertEquals(new BigDecimal("800.00"), cuenta.getSaldo());
        assertEquals(new BigDecimal("700.00"), destino.getSaldo());
    }

    @Test
    void transferir_destinoNoEncontrado_throws() {
        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByNumeroCuenta("99999999")).thenReturn(Optional.empty());

        TransferenciaSolicitudDTO dto = transferenciaDTO(1L, "99999999", "100.00");
        assertThrows(CuentaNoEncontradaException.class,
                () -> service.transferir(dto, "clienteTest"));
    }

    @Test
    void transferir_usuarioNoEncontrado_throws() {
        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.empty());

        TransferenciaSolicitudDTO dto = transferenciaDTO(1L, "55667788", "100.00");
        assertThrows(AutenticacionFallidaException.class,
                () -> service.transferir(dto, "clienteTest"));
    }

    @Test
    void transferir_origenNoPertenece_throws() {
        Cuenta destino = cuentaActivaAux(2L, "55667788");
        when(cuentaRepository.findByNumeroCuenta("55667788")).thenReturn(Optional.of(destino));
        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 3L))
                .thenReturn(Optional.empty());

        TransferenciaSolicitudDTO dto = transferenciaDTO(1L, "55667788", "100.00");
        assertThrows(AccesoNoAutorizadoException.class,
                () -> service.transferir(dto, "clienteTest"));
    }

    @Test
    void transferir_origenBloqueado_throws() {
        cuenta.setEstado(EstadoCuenta.BLOQUEADA);
        Cuenta destino = cuentaActivaAux(2L, "55667788");

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

    // ── obtenerMovimientosPorFecha ────────────────────────────────────────────

    @Test
    void obtenerMovimientosPorFecha_exitoso_retornaLista() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(7);
        LocalDateTime fin    = LocalDateTime.now();

        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 3L)).thenReturn(Optional.of(cuenta));
        when(movimientoRepository.findByCuenta_IdCuentaAndFechaBetweenOrderByFechaDesc(1L, inicio, fin))
                .thenReturn(Collections.emptyList());
        when(transferenciaRepository.findByCuentaIdAndFechaBetweenOrderByFechaDesc(1L, inicio, fin))
                .thenReturn(Collections.emptyList());
        when(transferenciaExternaRepository.findByCuentaOrigen_IdCuentaAndFechaBetweenOrderByFechaDesc(1L, inicio, fin))
                .thenReturn(Collections.emptyList());
        when(transferenciaInternacionalRepository.findByCuentaOrigen_IdCuentaAndFechaBetweenOrderByFechaDesc(1L, inicio, fin))
                .thenReturn(Collections.emptyList());
        when(transaccionMapper.aListaDTOUnificada(any(), any(), eq(1L), any(), any())).thenReturn(Collections.emptyList());

        List<MovimientoDTO> result = service.obtenerMovimientosPorFecha(1L, inicio, fin, "clienteTest");

        assertNotNull(result);
        verify(movimientoRepository).findByCuenta_IdCuentaAndFechaBetweenOrderByFechaDesc(1L, inicio, fin);
    }

    @Test
    void obtenerMovimientosPorFecha_sinAcceso_throws() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fin    = LocalDateTime.now();

        when(usuarioRepository.findByUsername("clienteTest")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(1L, 3L)).thenReturn(Optional.empty());

        assertThrows(AccesoNoAutorizadoException.class,
                () -> service.obtenerMovimientosPorFecha(1L, inicio, fin, "clienteTest"));
    }

    // ── helpers privados ─────────────────────────────────────────────────────

    private Cuenta cuentaActivaAux(Long id, String numero) {
        Cuenta c = new Cuenta();
        c.setIdCuenta(id);
        c.setNumeroCuenta(numero);
        c.setEstado(EstadoCuenta.ACTIVA);
        c.setSaldo(new BigDecimal("500.00"));
        return c;
    }

    private TransferenciaSolicitudDTO transferenciaDTO(Long idOrigen, String destino, String monto) {
        TransferenciaSolicitudDTO dto = new TransferenciaSolicitudDTO();
        dto.setIdCuentaOrigen(idOrigen);
        dto.setNumeroCuentaDestino(destino);
        dto.setMonto(new BigDecimal(monto));
        return dto;
    }
}
