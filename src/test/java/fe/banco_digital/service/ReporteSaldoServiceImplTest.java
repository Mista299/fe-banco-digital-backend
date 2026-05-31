package fe.banco_digital.service;

import fe.banco_digital.dto.ReporteConsolidadoDTO;
import fe.banco_digital.dto.ReporteEstadoCuentaDTO;
import fe.banco_digital.dto.SaldoTiempoRealDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.repository.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteSaldoServiceImplTest {

    @Mock CuentaRepository cuentaRepository;

    @InjectMocks ReporteSaldoServiceImpl service;

    Cliente cliente;
    Cuenta cuenta;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
        cliente.setNombre("Juan");

        cuenta = new Cuenta();
        cuenta.setIdCuenta(1L);
        cuenta.setNumeroCuenta("11112222");
        cuenta.setSaldo(new BigDecimal("1000.00"));
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setTipo(TipoCuenta.AHORROS);
        cuenta.setCliente(cliente);
    }

    @Test
    void obtenerConsolidadoGlobal_retornaTotales() {
        when(cuentaRepository.obtenerSaldoTotalSistema()).thenReturn(new BigDecimal("5000.00"));
        when(cuentaRepository.obtenerSaldoPorTipo(TipoCuenta.AHORROS)).thenReturn(new BigDecimal("3000.00"));
        when(cuentaRepository.obtenerSaldoPorTipo(TipoCuenta.CORRIENTE)).thenReturn(new BigDecimal("2000.00"));

        ReporteConsolidadoDTO resultado = service.obtenerConsolidadoGlobal();

        assertNotNull(resultado);
        assertEquals(new BigDecimal("5000.00"), resultado.getTotalSistema());
        assertEquals(new BigDecimal("3000.00"), resultado.getTotalAhorros());
        assertEquals(new BigDecimal("2000.00"), resultado.getTotalCorriente());
    }

    @Test
    void filtrarPorEstado_retornaLista() {
        when(cuentaRepository.findByEstadoConCliente(EstadoCuenta.ACTIVA)).thenReturn(List.of(cuenta));

        List<ReporteEstadoCuentaDTO> resultado = service.filtrarPorEstado(EstadoCuenta.ACTIVA);

        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getTitular());
    }

    @Test
    void filtrarPorEstado_sinCuentas_retornaVacio() {
        when(cuentaRepository.findByEstadoConCliente(EstadoCuenta.BLOQUEADA)).thenReturn(Collections.emptyList());

        List<ReporteEstadoCuentaDTO> resultado = service.filtrarPorEstado(EstadoCuenta.BLOQUEADA);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void obtenerSaldosTiempoReal_retornaLista() {
        when(cuentaRepository.findAll()).thenReturn(List.of(cuenta));

        List<SaldoTiempoRealDTO> resultado = service.obtenerSaldosTiempoReal();

        assertEquals(1, resultado.size());
        assertEquals(new BigDecimal("1000.00"), resultado.get(0).getSaldoDisponible());
    }

    @Test
    void filtrarPorRango_conMax_retornaFiltrado() {
        when(cuentaRepository.findBySaldoBetween(new BigDecimal("100.00"), new BigDecimal("2000.00")))
                .thenReturn(List.of(cuenta));

        List<SaldoTiempoRealDTO> resultado = service.filtrarPorRango(new BigDecimal("100.00"), new BigDecimal("2000.00"));

        assertEquals(1, resultado.size());
    }

    @Test
    void filtrarPorRango_sinMax_usaGreaterThan() {
        when(cuentaRepository.findBySaldoGreaterThan(new BigDecimal("500.00"))).thenReturn(List.of(cuenta));

        List<SaldoTiempoRealDTO> resultado = service.filtrarPorRango(new BigDecimal("500.00"), null);

        assertEquals(1, resultado.size());
    }
}
