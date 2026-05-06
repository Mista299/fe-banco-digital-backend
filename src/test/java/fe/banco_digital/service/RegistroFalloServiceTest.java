package fe.banco_digital.service;

import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoTransaccion;
import fe.banco_digital.entity.TipoTransaccion;
import fe.banco_digital.entity.Transaccion;
import fe.banco_digital.repository.TransaccionRepository;
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

    @Mock TransaccionRepository transaccionRepository;

    @InjectMocks RegistroFalloService service;

    @Test
    void registrarFallo_exitoso_guardaTransaccionFallida() {
        Cuenta origen = new Cuenta();
        origen.setIdCuenta(1L);
        Cuenta destino = new Cuenta();
        destino.setIdCuenta(2L);

        service.registrarFallo(origen, destino, TipoTransaccion.TRANSFERENCIA, new BigDecimal("50000"));

        ArgumentCaptor<Transaccion> captor = ArgumentCaptor.forClass(Transaccion.class);
        verify(transaccionRepository).save(captor.capture());
        assertEquals(EstadoTransaccion.FALLIDA, captor.getValue().getEstado());
        assertEquals(TipoTransaccion.TRANSFERENCIA, captor.getValue().getTipo());
    }

    @Test
    void registrarFallo_conDestinoNulo_guardaIgual() {
        Cuenta origen = new Cuenta();
        origen.setIdCuenta(1L);

        service.registrarFallo(origen, null, TipoTransaccion.RETIRO, new BigDecimal("10000"));

        verify(transaccionRepository).save(any());
    }

    @Test
    void registrarFallo_siRepositoryFalla_noLanzaExcepcion() {
        doThrow(new RuntimeException("DB error")).when(transaccionRepository).save(any());

        assertDoesNotThrow(() ->
            service.registrarFallo(null, null, TipoTransaccion.DEPOSITO, BigDecimal.ONE)
        );
    }
}
