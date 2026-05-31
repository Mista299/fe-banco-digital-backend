package fe.banco_digital.controller;

import fe.banco_digital.dto.ReporteConsolidadoDTO;
import fe.banco_digital.dto.ReporteEstadoCuentaDTO;
import fe.banco_digital.dto.SaldoTiempoRealDTO;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.service.ReporteSaldoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReporteSaldoControllerTest {

    @Mock ReporteSaldoService reporteSaldoService;
    @InjectMocks ReporteSaldoController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void obtenerConsolidado_retornaOk() throws Exception {
        ReporteConsolidadoDTO dto = new ReporteConsolidadoDTO(
                new BigDecimal("5000"), new BigDecimal("3000"), new BigDecimal("2000"));
        when(reporteSaldoService.obtenerConsolidadoGlobal()).thenReturn(dto);

        mockMvc.perform(get("/api/v1/reportes/saldos/consolidado"))
                .andExpect(status().isOk());
    }

    @Test
    void filtrarPorEstado_retornaOk() throws Exception {
        ReporteEstadoCuentaDTO item = new ReporteEstadoCuentaDTO("Juan", "11112222",
                new BigDecimal("1000"), "ACTIVA");
        when(reporteSaldoService.filtrarPorEstado(EstadoCuenta.ACTIVA)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/reportes/saldos/estado").param("estado", "ACTIVA"))
                .andExpect(status().isOk());
    }

    @Test
    void filtrarPorEstado_vacio_retornaOk() throws Exception {
        when(reporteSaldoService.filtrarPorEstado(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/reportes/saldos/estado").param("estado", "BLOQUEADA"))
                .andExpect(status().isOk());
    }

    @Test
    void filtrarPorRango_retornaOk() throws Exception {
        SaldoTiempoRealDTO item = new SaldoTiempoRealDTO(1L, new BigDecimal("1000"),
                new BigDecimal("1000"), "ACTIVA", "AHORROS");
        when(reporteSaldoService.filtrarPorRango(any(), any())).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/reportes/saldos/rango")
                        .param("min", "100")
                        .param("max", "5000"))
                .andExpect(status().isOk());
    }

    @Test
    void filtrarPorRango_sinMax_retornaOk() throws Exception {
        when(reporteSaldoService.filtrarPorRango(any(), isNull())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/reportes/saldos/rango").param("min", "500"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerTiempoReal_retornaOk() throws Exception {
        SaldoTiempoRealDTO item = new SaldoTiempoRealDTO(1L, new BigDecimal("1000"),
                new BigDecimal("1000"), "ACTIVA", "AHORROS");
        when(reporteSaldoService.obtenerSaldosTiempoReal()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/reportes/saldos/tiempo-real"))
                .andExpect(status().isOk());
    }
}
