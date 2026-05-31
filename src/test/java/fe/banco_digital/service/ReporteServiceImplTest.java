package fe.banco_digital.service;

import fe.banco_digital.dto.ReporteResumenDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceImplTest {

    @Mock MovimientoRepository movimientoRepository;
    @Mock TransferenciaRepository transferenciaRepository;
    @Mock TransferenciaExternaRepository transferenciaExternaRepository;
    @Mock TransferenciaInternacionalRepository transferenciaInternacionalRepository;

    @InjectMocks ReporteServiceImpl service;

    LocalDateTime inicio = LocalDateTime.now().minusDays(7);
    LocalDateTime fin = LocalDateTime.now();

    @Test
    void generarReporte_sinTransacciones_retornaVacio() {
        when(movimientoRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());
        when(transferenciaRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());
        when(transferenciaExternaRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());
        when(transferenciaInternacionalRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());

        ReporteResumenDTO resultado = service.generarReporte(inicio, fin);

        assertNotNull(resultado);
        assertTrue(resultado.getTransacciones().isEmpty());
        assertEquals(BigDecimal.ZERO, resultado.getVolumenTotal());
    }

    @Test
    void generarReporte_conMovimientos_calculaVolumen() {
        Cuenta cuentaRef = new Cuenta();
        cuentaRef.setNumeroCuenta("11112222");

        Movimiento mov = new Movimiento();
        mov.setIdMovimiento(1L);
        mov.setMonto(new BigDecimal("200.00"));
        mov.setEstado(EstadoMovimiento.EXITOSO);
        mov.setTipo(TipoMovimiento.DEPOSITO);
        mov.setFecha(LocalDateTime.now());
        mov.setCuenta(cuentaRef);

        when(movimientoRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(List.of(mov));
        when(transferenciaRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());
        when(transferenciaExternaRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());
        when(transferenciaInternacionalRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());

        ReporteResumenDTO resultado = service.generarReporte(inicio, fin);

        assertEquals(1, resultado.getTransacciones().size());
        assertEquals(new BigDecimal("200.00"), resultado.getVolumenTotal());
    }

    @Test
    void generarReporte_conTransferencia_incluyeEnReporte() {
        Cuenta origen = new Cuenta(); origen.setNumeroCuenta("11112222");
        Cuenta destino = new Cuenta(); destino.setNumeroCuenta("33334444");

        Transferencia transf = new Transferencia();
        transf.setIdTransferencia(1L);
        transf.setCuentaOrigen(origen);
        transf.setCuentaDestino(destino);
        transf.setMonto(new BigDecimal("150.00"));
        transf.setEstado(EstadoTransferencia.EXITOSA);
        transf.setFecha(LocalDateTime.now());

        when(movimientoRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());
        when(transferenciaRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(List.of(transf));
        when(transferenciaExternaRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());
        when(transferenciaInternacionalRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());

        ReporteResumenDTO resultado = service.generarReporte(inicio, fin);

        assertEquals(1, resultado.getTransacciones().size());
        assertEquals(new BigDecimal("150.00"), resultado.getVolumenTotal());
    }

    @Test
    void generarReporte_conTransferenciaExterna_incluyeEnReporte() {
        Cuenta origen = new Cuenta(); origen.setNumeroCuenta("11112222");

        TransferenciaExterna ext = new TransferenciaExterna();
        ext.setIdTransfExt(1L);
        ext.setCuentaOrigen(origen);
        ext.setNumeroCuentaDestino("EXT999");
        ext.setMonto(new BigDecimal("300.00"));
        ext.setEstado(EstadoTransferenciaExterna.EXITOSA);
        ext.setFecha(LocalDateTime.now());

        when(movimientoRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());
        when(transferenciaRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());
        when(transferenciaExternaRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(List.of(ext));
        when(transferenciaInternacionalRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());

        ReporteResumenDTO resultado = service.generarReporte(inicio, fin);

        assertEquals(1, resultado.getTransacciones().size());
    }

    @Test
    void exportarCSV_retornaBytes() {
        when(movimientoRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());
        when(transferenciaRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());
        when(transferenciaExternaRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());
        when(transferenciaInternacionalRepository.findByFechaBetweenOrderByFechaDesc(any(), any())).thenReturn(Collections.emptyList());

        byte[] csv = service.exportarCSV(inicio, fin);

        assertNotNull(csv);
        assertTrue(csv.length > 0);
        String contenido = new String(csv);
        assertTrue(contenido.contains("ID_TRANSACCION"));
    }
}
