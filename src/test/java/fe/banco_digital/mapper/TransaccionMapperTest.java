package fe.banco_digital.mapper;

import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.TipoTransaccion;
import fe.banco_digital.entity.Transaccion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransaccionMapperTest {

    private final TransaccionMapper mapper = new TransaccionMapper();

    @Test
    void aMovimientoDTO_esOrigen_montoNegativo() {
        Cuenta origen = new Cuenta();
        origen.setIdCuenta(1L);

        Transaccion t = new Transaccion();
        t.setTipo(TipoTransaccion.RETIRO);
        t.setMonto(new BigDecimal("300.00"));
        t.setCuentaOrigen(origen);
        t.setFecha(LocalDateTime.now());

        MovimientoDTO dto = mapper.aMovimientoDTO(t, 1L);

        assertEquals(new BigDecimal("-300.00"), dto.getMonto());
        assertEquals("RETIRO", dto.getConcepto());
    }

    @Test
    void aMovimientoDTO_esDestino_montoPositivo() {
        Cuenta destino = new Cuenta();
        destino.setIdCuenta(2L);

        Transaccion t = new Transaccion();
        t.setTipo(TipoTransaccion.DEPOSITO);
        t.setMonto(new BigDecimal("500.00"));
        t.setCuentaOrigen(null);
        t.setCuentaDestino(destino);
        t.setFecha(LocalDateTime.now());

        MovimientoDTO dto = mapper.aMovimientoDTO(t, 2L);

        assertEquals(new BigDecimal("500.00"), dto.getMonto());
        assertEquals("DEPOSITO", dto.getConcepto());
    }

    @Test
    void aMovimientoDTO_transferenciaRecibida_montoPositivo() {
        Cuenta origen = new Cuenta();
        origen.setIdCuenta(1L);
        Cuenta destino = new Cuenta();
        destino.setIdCuenta(2L);

        Transaccion t = new Transaccion();
        t.setTipo(TipoTransaccion.TRANSFERENCIA);
        t.setMonto(new BigDecimal("150.00"));
        t.setCuentaOrigen(origen);
        t.setCuentaDestino(destino);
        t.setFecha(LocalDateTime.now());

        MovimientoDTO dto = mapper.aMovimientoDTO(t, 2L); // consultado como destino

        assertEquals(new BigDecimal("150.00"), dto.getMonto());
    }

    @Test
    void aListaDTO_variosMovimientos_retornaListaCompleta() {
        Transaccion t1 = new Transaccion();
        t1.setTipo(TipoTransaccion.DEPOSITO);
        t1.setMonto(new BigDecimal("100.00"));
        t1.setFecha(LocalDateTime.now());

        Cuenta origen = new Cuenta();
        origen.setIdCuenta(5L);
        Transaccion t2 = new Transaccion();
        t2.setTipo(TipoTransaccion.RETIRO);
        t2.setCuentaOrigen(origen);
        t2.setMonto(new BigDecimal("50.00"));
        t2.setFecha(LocalDateTime.now());

        List<MovimientoDTO> result = mapper.aListaDTO(List.of(t1, t2), 5L);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("-50.00"), result.get(1).getMonto());
    }
}
