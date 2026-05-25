package fe.banco_digital.service;

import fe.banco_digital.dto.ConfirmacionSwiftSolicitudDTO;
import fe.banco_digital.dto.RechazoSwiftSolicitudDTO;
import fe.banco_digital.dto.TransferenciaInternacionalResponseDTO;
import fe.banco_digital.dto.TransferenciaInternacionalSolicitudDTO;
import fe.banco_digital.dto.ValidacionTransaccionResponseDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.EstadoTransferenciaInternacional;
import fe.banco_digital.entity.TipoMovimiento;
import fe.banco_digital.entity.TransferenciaInternacional;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.exception.OperacionNoPermitidaException;
import fe.banco_digital.exception.SaldoInsuficienteException;
import fe.banco_digital.exception.TransaccionNoEncontradaException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.MovimientoRepository;
import fe.banco_digital.repository.TransferenciaInternacionalRepository;
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
class TransferenciaInternacionalServiceImplTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock CuentaRepository cuentaRepository;
    @Mock TransferenciaInternacionalRepository tiRepository;
    @Mock MovimientoRepository movimientoRepository;
    @Mock MotorValidacionService motorValidacionService;
    @Mock RegistroFalloService registroFalloService;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks TransferenciaInternacionalServiceImpl service;

    Cliente cliente;
    Usuario usuario;
    Cuenta origen;
    TransferenciaInternacional tiPendiente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setIdCliente(1L);

        usuario = new Usuario();
        usuario.setIdUsuario(10L);
        usuario.setUsername("testuser");
        usuario.setCliente(cliente);

        origen = new Cuenta();
        origen.setIdCuenta(5L);
        origen.setNumeroCuenta("00050001");
        origen.setEstado(EstadoCuenta.ACTIVA);
        origen.setSaldo(new BigDecimal("1000000.00"));
        origen.setCliente(cliente);

        tiPendiente = new TransferenciaInternacional();
        tiPendiente.setCuentaOrigen(origen);
        tiPendiente.setMontoUsd(new BigDecimal("100.00"));
        tiPendiente.setTasaCambio(new BigDecimal("4200.000000"));
        tiPendiente.setMontoCop(new BigDecimal("420000.0000"));
        tiPendiente.setEstado(EstadoTransferenciaInternacional.PENDIENTE_PROCESAMIENTO);
        tiPendiente.setReferenciaSwift("SWIFT-TEST-001");
        tiPendiente.setFecha(LocalDateTime.now());
    }

    private ValidacionTransaccionResponseDTO autorizada() {
        return new ValidacionTransaccionResponseDTO(
                true, "OK", "Autorizado", 5L, "ACTIVA", new BigDecimal("1000000.00"));
    }

    private ValidacionTransaccionResponseDTO saldoInsuficiente() {
        return new ValidacionTransaccionResponseDTO(
                false, "SALDO_INSUFICIENTE", "Saldo insuficiente", 5L, "ACTIVA", BigDecimal.ZERO);
    }

    private ValidacionTransaccionResponseDTO bloqueada() {
        return new ValidacionTransaccionResponseDTO(
                false, "CUENTA_BLOQUEADA", "Cuenta bloqueada", 5L, "BLOQUEADA", BigDecimal.ZERO);
    }

    private TransferenciaInternacionalSolicitudDTO solicitudValida() {
        TransferenciaInternacionalSolicitudDTO dto = new TransferenciaInternacionalSolicitudDTO();
        dto.setIdCuentaOrigen(5L);
        dto.setBancoDestino("Citibank");
        dto.setCodigoSwift("CITIUS33");
        dto.setPaisDestino("Estados Unidos");
        dto.setTipoCuentaDestino("CHECKING");
        dto.setIbanCuentaDestino("US64SVBK0000000000000000");
        dto.setTipoDocumentoReceptor("PASSPORT");
        dto.setNumeroDocumentoReceptor("A123456");
        dto.setNombreReceptor("John Smith");
        dto.setMontoUsd(new BigDecimal("100.00"));
        dto.setTasaCambio(new BigDecimal("4200"));
        dto.setMoneda("USD");
        return dto;
    }

    private void mockAccesoYLock() {
        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(5L, 1L)).thenReturn(Optional.of(origen));
        when(cuentaRepository.findByIdCuentaConLock(5L)).thenReturn(Optional.of(origen));
    }

    // ── iniciarTransferencia ─────────────────────────────────────────────────

    @Test
    void iniciarTransferencia_exitoso_debitaCopYGuarda() {
        mockAccesoYLock();
        when(motorValidacionService.validarCuentaParaDebito(any(), any())).thenReturn(autorizada());
        when(tiRepository.save(any())).thenReturn(tiPendiente);

        TransferenciaInternacionalResponseDTO result = service.iniciarTransferencia(solicitudValida(), "testuser");

        assertNotNull(result);
        assertEquals(0, new BigDecimal("580000.00").compareTo(origen.getSaldo()));
        verify(cuentaRepository).save(origen);
        verify(tiRepository).save(any());
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
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(5L, 1L)).thenReturn(Optional.empty());

        assertThrows(AccesoNoAutorizadoException.class,
                () -> service.iniciarTransferencia(solicitudValida(), "testuser"));
    }

    // ── registrarConfirmacionSwift ────────────────────────────────────────────

    @Test
    void registrarConfirmacionSwift_exitoso_cambiaEstadoAExitosa() {
        when(tiRepository.findById(1L)).thenReturn(Optional.of(tiPendiente));
        when(tiRepository.save(any())).thenReturn(tiPendiente);
        when(cuentaRepository.findById(5L)).thenReturn(Optional.of(origen));
        when(usuarioRepository.findByCliente_IdCliente(1L)).thenReturn(Optional.of(usuario));

        ConfirmacionSwiftSolicitudDTO confirmacion = new ConfirmacionSwiftSolicitudDTO();
        confirmacion.setReferenciaConfirmacion("SWIFT-CONF-001");

        TransferenciaInternacionalResponseDTO result = service.registrarConfirmacionSwift(1L, confirmacion);

        assertNotNull(result);
        assertEquals(EstadoTransferenciaInternacional.EXITOSA, tiPendiente.getEstado());
        assertEquals("SWIFT-CONF-001", tiPendiente.getReferenciaSwift());
    }

    @Test
    void registrarConfirmacionSwift_noEncontrada_throws() {
        when(tiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TransaccionNoEncontradaException.class,
                () -> service.registrarConfirmacionSwift(99L, new ConfirmacionSwiftSolicitudDTO()));
    }

    @Test
    void registrarConfirmacionSwift_noEstaPendiente_throws() {
        tiPendiente.setEstado(EstadoTransferenciaInternacional.EXITOSA);
        when(tiRepository.findById(1L)).thenReturn(Optional.of(tiPendiente));

        assertThrows(OperacionNoPermitidaException.class,
                () -> service.registrarConfirmacionSwift(1L, new ConfirmacionSwiftSolicitudDTO()));
    }

    // ── registrarRechazoSwift ─────────────────────────────────────────────────

    @Test
    void registrarRechazoSwift_exitoso_reversaSaldoYGuardaMovimiento() {
        when(tiRepository.findById(1L)).thenReturn(Optional.of(tiPendiente));
        when(cuentaRepository.findByIdCuentaConLock(5L)).thenReturn(Optional.of(origen));
        when(tiRepository.save(any())).thenReturn(tiPendiente);
        when(usuarioRepository.findByCliente_IdCliente(1L)).thenReturn(Optional.of(usuario));

        RechazoSwiftSolicitudDTO rechazo = new RechazoSwiftSolicitudDTO();
        rechazo.setMotivo("Código SWIFT inválido");

        TransferenciaInternacionalResponseDTO result = service.registrarRechazoSwift(1L, rechazo);

        assertNotNull(result);
        assertEquals(EstadoTransferenciaInternacional.REVERSADA, tiPendiente.getEstado());
        assertEquals(0, new BigDecimal("1420000.00").compareTo(origen.getSaldo()));
        verify(movimientoRepository).save(any());
    }

    @Test
    void registrarRechazoSwift_noEncontrada_throws() {
        when(tiRepository.findById(99L)).thenReturn(Optional.empty());

        RechazoSwiftSolicitudDTO rechazo = new RechazoSwiftSolicitudDTO();
        rechazo.setMotivo("motivo");

        assertThrows(TransaccionNoEncontradaException.class,
                () -> service.registrarRechazoSwift(99L, rechazo));
    }

    @Test
    void registrarRechazoSwift_noEstaPendiente_throws() {
        tiPendiente.setEstado(EstadoTransferenciaInternacional.EXITOSA);
        when(tiRepository.findById(1L)).thenReturn(Optional.of(tiPendiente));

        RechazoSwiftSolicitudDTO rechazo = new RechazoSwiftSolicitudDTO();
        rechazo.setMotivo("motivo");

        assertThrows(OperacionNoPermitidaException.class,
                () -> service.registrarRechazoSwift(1L, rechazo));
    }

    // ── consultarTransferencia ────────────────────────────────────────────────

    @Test
    void consultarTransferencia_exitoso_retornaDTO() {
        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(tiRepository.findById(1L)).thenReturn(Optional.of(tiPendiente));
        when(cuentaRepository.findById(5L)).thenReturn(Optional.of(origen));

        TransferenciaInternacionalResponseDTO result = service.consultarTransferencia(1L, "testuser");

        assertNotNull(result);
    }

    @Test
    void consultarTransferencia_ajena_throws() {
        Cliente otroCliente = new Cliente();
        otroCliente.setIdCliente(99L);
        Usuario otroUsuario = new Usuario();
        otroUsuario.setCliente(otroCliente);

        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(otroUsuario));
        when(tiRepository.findById(1L)).thenReturn(Optional.of(tiPendiente));

        assertThrows(AccesoNoAutorizadoException.class,
                () -> service.consultarTransferencia(1L, "testuser"));
    }

    @Test
    void consultarTransferencia_noEncontrada_throws() {
        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(tiRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TransaccionNoEncontradaException.class,
                () -> service.consultarTransferencia(99L, "testuser"));
    }
}
