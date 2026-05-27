package fe.banco_digital.mapper;

import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoMovimiento;
import fe.banco_digital.entity.EstadoTransferencia;
import fe.banco_digital.entity.Movimiento;
import fe.banco_digital.entity.TipoMovimiento;
import fe.banco_digital.entity.Transferencia;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransaccionMapperTest {

    private final TransaccionMapper mapper = new TransaccionMapper();

    @Test
    void aMovimientoDTO_retiro_montoNegativo() {
        Movimiento m = new Movimiento();
        m.setTipo(TipoMovimiento.RETIRO);
        m.setMonto(new BigDecimal("300.00"));
        m.setEstado(EstadoMovimiento.EXITOSO);
        m.setFecha(LocalDateTime.now());

        MovimientoDTO dto = mapper.aMovimientoDTO(m);

        assertEquals(new BigDecimal("-300.00"), dto.getMonto());
        assertEquals("RETIRO", dto.getConcepto());
    }

    @Test
    void aMovimientoDTO_deposito_montoPositivo() {
        Movimiento m = new Movimiento();
        m.setTipo(TipoMovimiento.DEPOSITO);
        m.setMonto(new BigDecimal("500.00"));
        m.setEstado(EstadoMovimiento.EXITOSO);
        m.setFecha(LocalDateTime.now());

        MovimientoDTO dto = mapper.aMovimientoDTO(m);

        assertEquals(new BigDecimal("500.00"), dto.getMonto());
        assertEquals("DEPOSITO", dto.getConcepto());
    }

    @Test
    void aMovimientoDTO_transferenciaRecibida_montoPositivo() {
        Cuenta origen = new Cuenta();
        origen.setIdCuenta(1L);
        Cuenta destino = new Cuenta();
        destino.setIdCuenta(2L);

        Transferencia t = new Transferencia();
        t.setMonto(new BigDecimal("150.00"));
        t.setCuentaOrigen(origen);
        t.setCuentaDestino(destino);
        t.setEstado(EstadoTransferencia.EXITOSA);
        t.setFecha(LocalDateTime.now());

        MovimientoDTO dto = mapper.aMovimientoDTO(t, 2L); // consultado como destino

        assertEquals(new BigDecimal("150.00"), dto.getMonto());
    }

    @Test
    void aMovimientoDTO_transferenciaEnviada_montoNegativo() {
        Cuenta origen = new Cuenta();
        origen.setIdCuenta(1L);
        Cuenta destino = new Cuenta();
        destino.setIdCuenta(2L);

        Transferencia t = new Transferencia();
        t.setMonto(new BigDecimal("150.00"));
        t.setCuentaOrigen(origen);
        t.setCuentaDestino(destino);
        t.setEstado(EstadoTransferencia.EXITOSA);
        t.setFecha(LocalDateTime.now());

        MovimientoDTO dto = mapper.aMovimientoDTO(t, 1L); // consultado como origen

        assertEquals(new BigDecimal("-150.00"), dto.getMonto());
    }

    @Test
    void aListaDTOUnificada_variosMovimientos_retornaListaCompleta() {
        Movimiento m1 = new Movimiento();
        m1.setTipo(TipoMovimiento.DEPOSITO);
        m1.setMonto(new BigDecimal("100.00"));
        m1.setEstado(EstadoMovimiento.EXITOSO);
        m1.setFecha(LocalDateTime.now().minusMinutes(2));

        Movimiento m2 = new Movimiento();
        m2.setTipo(TipoMovimiento.RETIRO);
        m2.setMonto(new BigDecimal("50.00"));
        m2.setEstado(EstadoMovimiento.EXITOSO);
        m2.setFecha(LocalDateTime.now().minusMinutes(1));

        List<MovimientoDTO> result = mapper.aListaDTOUnificada(
                List.of(m1, m2),
                Collections.emptyList(),
                5L,
                Collections.emptyList(),
                Collections.emptyList());

        assertEquals(2, result.size());
        // m2 es más reciente, aparece primero
        assertEquals(new BigDecimal("-50.00"), result.get(0).getMonto());
    }
}
