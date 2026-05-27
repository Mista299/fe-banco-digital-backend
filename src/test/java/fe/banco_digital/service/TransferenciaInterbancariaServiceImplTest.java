package fe.banco_digital.service;

import fe.banco_digital.dto.ConfirmacionAchSolicitudDTO;
import fe.banco_digital.dto.RechazoAchSolicitudDTO;
import fe.banco_digital.dto.TransferenciaInterbancariaResponseDTO;
import fe.banco_digital.dto.TransferenciaInterbancariaSolicitudDTO;
import fe.banco_digital.dto.ValidacionTransaccionResponseDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.EstadoTransferenciaExterna;
import fe.banco_digital.entity.TipoMovimiento;
import fe.banco_digital.entity.TransferenciaExterna;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.OperacionNoPermitidaException;
import fe.banco_digital.exception.SaldoInsuficienteException;
import fe.banco_digital.exception.TransaccionNoEncontradaException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.MovimientoRepository;
import fe.banco_digital.repository.TransferenciaExternaRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferenciaInterbancariaServiceImplTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock CuentaRepository cuentaRepository;
    @Mock TransferenciaExternaRepository teRepository;
    @Mock MovimientoRepository movimientoRepository;
    @Mock MotorValidacionService motorValidacionService;
    @Mock RegistroFalloService registroFalloService;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks TransferenciaInterbancariaServiceImpl service;

    Cliente cliente;
    Usuario usuario;
    Cuenta origen;
    TransferenciaExterna tePendiente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(2L);

        usuario = new Usuario();
        usuario.setIdUsuario(20L);
        usuario.setUsername("testuser");
        usuario.setCliente(cliente);

        origen = new Cuenta();
        origen.setIdCuenta(7L);
        origen.setNumeroCuenta("00070001");
        origen.setEstado(EstadoCuenta.ACTIVA);
        origen.setSaldo(new BigDecimal("500000.00"));
        origen.setCliente(cliente);

        tePendiente = new TransferenciaExterna();
        tePendiente.setCuentaOrigen(origen);
        tePendiente.setBancoDestino("Bancolombia");
        tePendiente.setMonto(new BigDecimal("100000.00"));
        tePendiente.setEstado(EstadoTransferenciaExterna.PENDIENTE_PROCESAMIENTO);
        tePendiente.setReferenciaExterna("ACH-TEST-001");
        tePendiente.setFecha(LocalDateTime.now());
    }

    private ValidacionTransaccionResponseDTO autorizada() {
        return new ValidacionTransaccionResponseDTO(
                true, "OK", "Autorizado", 7L, "ACTIVA", new BigDecimal("500000.00"));
    }

    private ValidacionTransaccionResponseDTO saldoInsuficiente() {
        return new ValidacionTransaccionResponseDTO(
                false, "SALDO_INSUFICIENTE", "Saldo insuficiente", 7L, "ACTIVA", BigDecimal.ZERO);
    }

    private ValidacionTransaccionResponseDTO bloqueada() {
        return new ValidacionTransaccionResponseDTO(
                false, "CUENTA_BLOQUEADA", "Cuenta bloqueada", 7L, "BLOQUEADA", BigDecimal.ZERO);
    }

    private TransferenciaInterbancariaSolicitudDTO solicitudValida() {
        TransferenciaInterbancariaSolicitudDTO dto = new TransferenciaInterbancariaSolicitudDTO();
        dto.setIdCuentaOrigen(7L);
        dto.setBancoDestino("Bancolombia");
        dto.setTipoCuentaDestino("AHORROS");
        dto.setNumeroCuentaDestino("12345678901");
        dto.setTipoDocumentoReceptor("CC");
        dto.setNumeroDocumentoReceptor("9876543210");
        dto.setNombreReceptor("Pedro Suárez");
        dto.setMonto(new BigDecimal("100000.00"));
        return dto;
    }

    private void mockAccesoYLock() {
        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(7L, 2L)).thenReturn(Optional.of(origen));
        when(cuentaRepository.findByIdCuentaConLock(7L)).thenReturn(Optional.of(origen));
    }

    // ── iniciarTransferencia ─────────────────────────────────────────────────

    @Test
    void iniciarTransferencia_exitoso_debitaYGuarda() {
        mockAccesoYLock();
        when(motorValidacionService.validarCuentaParaDebito(any(), any())).thenReturn(autorizada());
        when(teRepository.save(any())).thenReturn(tePendiente);

        TransferenciaInterbancariaResponseDTO result = service.iniciarTransferencia(solicitudValida(), "testuser");

        assertNotNull(result);
        assertEquals(new BigDecimal("400000.00"), origen.getSaldo());
        verify(cuentaRepository).save(origen);
        verify(teRepository).save(any());
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void iniciarTransferencia_saldoInsuficiente_registraFalloYThrows() {
        mockAccesoYLock();
        when(motorValidacionService.validarCuentaParaDebito(any(), any())).thenReturn(saldoInsuficiente());

        assertThrows(SaldoInsuficienteException.class,
                () -> service.iniciarTransferencia(solicitudValida(), "testuser"));
        verify(registroFalloService).registrarFalloMovimiento(eq(origen), eq(TipoMovimiento.RETIRO), any());
    }

    @Test
    void iniciarTransferencia_cuentaBloqueada_throws() {
        mockAccesoYLock();
        when(motorValidacionService.validarCuentaParaDebito(any(), any())).thenReturn(bloqueada());

        assertThrows(OperacionNoPermitidaException.class,
                () -> service.iniciarTransferencia(solicitudValida(), "testuser"));
    }

    @Test
    void iniciarTransferencia_usuarioNoEncontrado_throws() {
        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(AutenticacionFallidaException.class,
                () -> service.iniciarTransferencia(solicitudValida(), "testuser"));
    }

    @Test
    void iniciarTransferencia_cuentaAjena_throws() {
        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(7L, 2L)).thenReturn(Optional.empty());

        assertThrows(AccesoNoAutorizadoException.class,
                () -> service.iniciarTransferencia(solicitudValida(), "testuser"));
    }

    // ── registrarConfirmacionAch ──────────────────────────────────────────────

    @Test
    void registrarConfirmacionAch_exitoso_cambiaEstadoAExitosa() {
        when(teRepository.findById(1L)).thenReturn(Optional.of(tePendiente));
        when(teRepository.save(any())).thenReturn(tePendiente);
        when(cuentaRepository.findById(7L)).thenReturn(Optional.of(origen));
        when(usuarioRepository.findByCliente_IdCliente(2L)).thenReturn(Optional.of(usuario));

        ConfirmacionAchSolicitudDTO confirmacion = new ConfirmacionAchSolicitudDTO();
        confirmacion.setReferenciaConfirmacion("ACH-CONF-001");

        TransferenciaInterbancariaResponseDTO result = service.registrarConfirmacionAch(1L, confirmacion);

        assertNotNull(result);
        assertEquals(EstadoTransferenciaExterna.EXITOSA, tePendiente.getEstado());
    }

    @Test
    void registrarConfirmacionAch_noEncontrada_throws() {
        when(teRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TransaccionNoEncontradaException.class,
                () -> service.registrarConfirmacionAch(99L, new ConfirmacionAchSolicitudDTO()));
    }

    @Test
    void registrarConfirmacionAch_noEstaPendiente_throws() {
        tePendiente.setEstado(EstadoTransferenciaExterna.EXITOSA);
        when(teRepository.findById(1L)).thenReturn(Optional.of(tePendiente));

        assertThrows(OperacionNoPermitidaException.class,
                () -> service.registrarConfirmacionAch(1L, new ConfirmacionAchSolicitudDTO()));
    }

    // ── registrarRechazoAch ───────────────────────────────────────────────────

    @Test
    void registrarRechazoAch_exitoso_reversaSaldoYGuardaMovimiento() {
        when(teRepository.findById(1L)).thenReturn(Optional.of(tePendiente));
        when(cuentaRepository.findByIdCuentaConLock(7L)).thenReturn(Optional.of(origen));
        when(teRepository.save(any())).thenReturn(tePendiente);
        when(usuarioRepository.findByCliente_IdCliente(2L)).thenReturn(Optional.of(usuario));

        RechazoAchSolicitudDTO rechazo = new RechazoAchSolicitudDTO();
        rechazo.setMotivo("Cuenta destino no existe");

        TransferenciaInterbancariaResponseDTO result = service.registrarRechazoAch(1L, rechazo);

        assertNotNull(result);
        assertEquals(EstadoTransferenciaExterna.REVERSADA, tePendiente.getEstado());
        assertEquals(new BigDecimal("600000.00"), origen.getSaldo());
        verify(movimientoRepository).save(any());
    }

    @Test
    void registrarRechazoAch_noEncontrada_throws() {
        when(teRepository.findById(99L)).thenReturn(Optional.empty());

        RechazoAchSolicitudDTO rechazo = new RechazoAchSolicitudDTO();
        rechazo.setMotivo("motivo");

        assertThrows(TransaccionNoEncontradaException.class,
                () -> service.registrarRechazoAch(99L, rechazo));
    }

    @Test
    void registrarRechazoAch_noEstaPendiente_throws() {
        tePendiente.setEstado(EstadoTransferenciaExterna.EXITOSA);
        when(teRepository.findById(1L)).thenReturn(Optional.of(tePendiente));

        RechazoAchSolicitudDTO rechazo = new RechazoAchSolicitudDTO();
        rechazo.setMotivo("motivo");

        assertThrows(OperacionNoPermitidaException.class,
                () -> service.registrarRechazoAch(1L, rechazo));
    }

    // ── consultarTransferencia ────────────────────────────────────────────────

    @Test
    void consultarTransferencia_exitoso_retornaDTO() {
        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(teRepository.findById(1L)).thenReturn(Optional.of(tePendiente));
        when(cuentaRepository.findById(7L)).thenReturn(Optional.of(origen));

        TransferenciaInterbancariaResponseDTO result = service.consultarTransferencia(1L, "testuser");

        assertNotNull(result);
    }

    @Test
    void consultarTransferencia_ajena_throws() {
        Cliente otroCliente = new Cliente();
        otroCliente.setIdCliente(99L);
        Usuario otroUsuario = new Usuario();
        otroUsuario.setCliente(otroCliente);

        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(otroUsuario));
        when(teRepository.findById(1L)).thenReturn(Optional.of(tePendiente));

        assertThrows(AccesoNoAutorizadoException.class,
                () -> service.consultarTransferencia(1L, "testuser"));
    }

    @Test
    void consultarTransferencia_noEncontrada_throws() {
        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(teRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TransaccionNoEncontradaException.class,
                () -> service.consultarTransferencia(99L, "testuser"));
    }
}
