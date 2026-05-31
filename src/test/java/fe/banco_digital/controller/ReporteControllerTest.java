package fe.banco_digital.controller;

import fe.banco_digital.dto.ReporteResumenDTO;
import fe.banco_digital.service.ReporteService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReporteControllerTest {

    @Mock ReporteService reporteService;
    @InjectMocks ReporteController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void generarReporte_retornaOk() throws Exception {
        when(reporteService.generarReporte(any(), any()))
                .thenReturn(new ReporteResumenDTO(Collections.emptyList(), BigDecimal.ZERO));

        mockMvc.perform(get("/api/v1/reportes")
                        .param("inicio", "2026-01-01T00:00:00")
                        .param("fin", "2026-05-31T23:59:59"))
                .andExpect(status().isOk());
    }

    @Test
    void exportarCSV_retornaOk() throws Exception {
        when(reporteService.exportarCSV(any(), any())).thenReturn("ID,MONTO".getBytes());

        mockMvc.perform(get("/api/v1/reportes/csv")
                        .param("inicio", "2026-01-01T00:00:00")
                        .param("fin", "2026-05-31T23:59:59"))
                .andExpect(status().isOk());
    }
}
