package fe.banco_digital.service;

import fe.banco_digital.dto.ComprobanteDepositoDTO;
import fe.banco_digital.dto.NotificacionDepositoDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.EstadoTransaccion;
import fe.banco_digital.entity.TipoTransaccion;
import fe.banco_digital.entity.Transaccion;
import fe.banco_digital.exception.DepositoRechazadoException;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositoGatewayServiceImplTest {

    @Mock private CuentaRepository cuentaRepository;
    @Mock private TransaccionRepository transaccionRepository;
    @Mock private RegistroFalloService registroFalloService;

    @InjectMocks
    private DepositoGatewayServiceImpl servicio;

    private Cuenta cuentaActiva;
    private NotificacionDepositoDTO notificacion;

    @BeforeEach
    void setUp() {
        cuentaActiva = new Cuenta();
        cuentaActiva.setNumeroCuenta("5001000001");
        cuentaActiva.setEstado(EstadoCuenta.ACTIVA);
        cuentaActiva.setSaldo(new BigDecimal("100000.00"));

        notificacion = new NotificacionDepositoDTO();
        notificacion.setNumeroCuenta("5001000001");
        notificacion.setMonto(new BigDecimal("50000.00"));
        notificacion.setReferenciaGateway("REF-001");
        notificacion.setCanalOrigen("PSE");
    }

    @Test
    void procesarNotificacion_acreditaSaldoYRetornaComprobante() {
        Transaccion transaccionGuardada = new Transaccion();
        transaccionGuardada.setIdTransaccion(1L);
        transaccionGuardada.setMonto(notificacion.getMonto());
        transaccionGuardada.setEstado(EstadoTransaccion.EXITOSA);
        transaccionGuardada.setFecha(LocalDateTime.now());

        when(cuentaRepository.findByNumeroCuentaConLock("5001000001"))
                .thenReturn(Optional.of(cuentaActiva));
        when(transaccionRepository.save(any())).thenReturn(transaccionGuardada);

        ComprobanteDepositoDTO comprobante = servicio.procesarNotificacion(notificacion);

        assertThat(comprobante.getNumeroOperacion()).isEqualTo(1L);
        assertThat(comprobante.getMonto()).isEqualByComparingTo("50000.00");
        assertThat(comprobante.getNumeroCuentaDestino()).isEqualTo("5001000001");
        assertThat(comprobante.getSaldoResultante()).isEqualByComparingTo("150000.00");
        assertThat(comprobante.getEstado()).isEqualTo("EXITOSA");
    }

    @Test
    void procesarNotificacion_guardaTransaccionConCamposCorrectos() {
        Transaccion transaccionGuardada = new Transaccion();
        transaccionGuardada.setIdTransaccion(1L);
        transaccionGuardada.setMonto(notificacion.getMonto());
        transaccionGuardada.setEstado(EstadoTransaccion.EXITOSA);
        transaccionGuardada.setFecha(LocalDateTime.now());

        when(cuentaRepository.findByNumeroCuentaConLock(any())).thenReturn(Optional.of(cuentaActiva));
        when(transaccionRepository.save(any())).thenReturn(transaccionGuardada);

        servicio.procesarNotificacion(notificacion);

        ArgumentCaptor<Transaccion> captor = ArgumentCaptor.forClass(Transaccion.class);
        verify(transaccionRepository).save(captor.capture());
        Transaccion guardada = captor.getValue();

        assertThat(guardada.getTipo()).isEqualTo(TipoTransaccion.DEPOSITO);
        assertThat(guardada.getMonto()).isEqualByComparingTo("50000.00");
        assertThat(guardada.getCuentaDestino()).isEqualTo(cuentaActiva);
        assertThat(guardada.getCuentaOrigen()).isNull();
    }

    @Test
    void procesarNotificacion_rechazaCuentaNoEncontrada() {
        when(cuentaRepository.findByNumeroCuentaConLock(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.procesarNotificacion(notificacion))
                .isInstanceOf(DepositoRechazadoException.class)
                .satisfies(ex -> {
                    var rechazo = ((DepositoRechazadoException) ex).getRechazo();
                    assertThat(rechazo.getMotivo()).isEqualTo("Cuenta no encontrada.");
                    assertThat(rechazo.isDevolucionSimulada()).isTrue();
                });

        verify(transaccionRepository, never()).save(any());
    }

    @Test
    void procesarNotificacion_rechazaCuentaBloqueada() {
        cuentaActiva.setEstado(EstadoCuenta.BLOQUEADA);
        when(cuentaRepository.findByNumeroCuentaConLock(any())).thenReturn(Optional.of(cuentaActiva));

        assertThatThrownBy(() -> servicio.procesarNotificacion(notificacion))
                .isInstanceOf(DepositoRechazadoException.class)
                .satisfies(ex -> {
                    var rechazo = ((DepositoRechazadoException) ex).getRechazo();
                    assertThat(rechazo.getMotivo()).isEqualTo("Cuenta bloqueada.");
                    assertThat(rechazo.isDevolucionSimulada()).isTrue();
                });

        verify(registroFalloService).registrarFallo(isNull(), eq(cuentaActiva),
                eq(TipoTransaccion.DEPOSITO), eq(notificacion.getMonto()));
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    void procesarNotificacion_rechazaCuentaCerrada() {
        cuentaActiva.setEstado(EstadoCuenta.INACTIVA);
        when(cuentaRepository.findByNumeroCuentaConLock(any())).thenReturn(Optional.of(cuentaActiva));

        assertThatThrownBy(() -> servicio.procesarNotificacion(notificacion))
                .isInstanceOf(DepositoRechazadoException.class)
                .satisfies(ex -> {
                    var rechazo = ((DepositoRechazadoException) ex).getRechazo();
                    assertThat(rechazo.getMotivo()).isEqualTo("Cuenta cerrada.");
                    assertThat(rechazo.isDevolucionSimulada()).isTrue();
                });

        verify(registroFalloService).registrarFallo(isNull(), eq(cuentaActiva),
                eq(TipoTransaccion.DEPOSITO), eq(notificacion.getMonto()));
        verify(transaccionRepository, never()).save(any());
    }

    @Test
    void procesarNotificacion_noModificaSaldoCuandoRechaza() {
        cuentaActiva.setEstado(EstadoCuenta.BLOQUEADA);
        BigDecimal saldoOriginal = cuentaActiva.getSaldo();
        when(cuentaRepository.findByNumeroCuentaConLock(any())).thenReturn(Optional.of(cuentaActiva));

        assertThatThrownBy(() -> servicio.procesarNotificacion(notificacion))
                .isInstanceOf(DepositoRechazadoException.class);

        assertThat(cuentaActiva.getSaldo()).isEqualByComparingTo(saldoOriginal);
        verify(cuentaRepository, never()).save(any());
    }
}
