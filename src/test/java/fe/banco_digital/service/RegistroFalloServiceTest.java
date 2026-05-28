package fe.banco_digital.service;

import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoMovimiento;
import fe.banco_digital.entity.EstadoTransferencia;
import fe.banco_digital.entity.Movimiento;
import fe.banco_digital.entity.TipoMovimiento;
import fe.banco_digital.entity.Transferencia;
import fe.banco_digital.repository.MovimientoRepository;
import fe.banco_digital.repository.TransferenciaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroFalloServiceTest {

    @Mock MovimientoRepository movimientoRepository;
    @Mock TransferenciaRepository transferenciaRepository;

    @InjectMocks RegistroFalloService service;

    @Test
    void registrarFalloMovimiento_exitoso_guardaMovimientoFallido() {
        Cuenta cuenta = new Cuenta();
        cuenta.setIdCuenta(1L);

        service.registrarFalloMovimiento(cuenta, TipoMovimiento.RETIRO, new BigDecimal("10000"));

        ArgumentCaptor<Movimiento> captor = ArgumentCaptor.forClass(Movimiento.class);
        verify(movimientoRepository).save(captor.capture());
        assertEquals(EstadoMovimiento.FALLIDO, captor.getValue().getEstado());
        assertEquals(TipoMovimiento.RETIRO, captor.getValue().getTipo());
    }

    @Test
    void registrarFalloTransferencia_exitoso_guardaTransferenciaFallida() {
        Cuenta origen = new Cuenta();
        origen.setIdCuenta(1L);
        Cuenta destino = new Cuenta();
        destino.setIdCuenta(2L);

        service.registrarFalloTransferencia(origen, destino, new BigDecimal("50000"));

        ArgumentCaptor<Transferencia> captor = ArgumentCaptor.forClass(Transferencia.class);
        verify(transferenciaRepository).save(captor.capture());
        assertEquals(EstadoTransferencia.FALLIDA, captor.getValue().getEstado());
    }

    @Test
    void registrarFalloMovimiento_conRepositoryFalla_noLanzaExcepcion() {
        doThrow(new RuntimeException("DB error")).when(movimientoRepository).save(any());

        assertDoesNotThrow(() ->
            service.registrarFalloMovimiento(null, TipoMovimiento.DEPOSITO, BigDecimal.ONE)
        );
    }

    @Test
    void registrarFalloTransferencia_conRepositoryFalla_noLanzaExcepcion() {
        doThrow(new RuntimeException("DB error")).when(transferenciaRepository).save(any());

        assertDoesNotThrow(() ->
            service.registrarFalloTransferencia(null, null, BigDecimal.ONE)
        );
    }
}
