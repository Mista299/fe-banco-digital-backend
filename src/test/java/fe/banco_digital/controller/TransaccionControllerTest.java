package fe.banco_digital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fe.banco_digital.dto.DepositoSolicitudDTO;
import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.dto.RetiroSolicitudDTO;
import fe.banco_digital.dto.TransaccionRespuestaDTO;
import fe.banco_digital.dto.TransferenciaSolicitudDTO;
import fe.banco_digital.service.TransaccionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransaccionControllerTest {

    @Mock TransaccionService transaccionService;

    @InjectMocks TransaccionController controller;

    MockMvc mockMvc;
    ObjectMapper mapper = new ObjectMapper();

    TransaccionRespuestaDTO respuestaBase;

    @BeforeEach
    void setUp() {
        User userDetails = new User("testuser", "pass", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();

        respuestaBase = new TransaccionRespuestaDTO(
                1L, "DEPOSITO", new BigDecimal("200.00"),
                new BigDecimal("1200.00"), "EXITOSO", LocalDateTime.now(), "Operación exitosa");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void depositar_retorna200YCallsService() throws Exception {
        DepositoSolicitudDTO dto = new DepositoSolicitudDTO();
        dto.setIdCuenta(1L);
        dto.setMonto(new BigDecimal("200.00"));

        when(transaccionService.depositar(any(), eq("testuser"))).thenReturn(respuestaBase);

        mockMvc.perform(post("/api/v1/transacciones/depositar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("DEPOSITO"));

        verify(transaccionService).depositar(any(), eq("testuser"));
    }

    @Test
    void retirar_retorna200YCallsService() throws Exception {
        RetiroSolicitudDTO dto = new RetiroSolicitudDTO();
        dto.setIdCuenta(1L);
        dto.setMonto(new BigDecimal("100.00"));

        TransaccionRespuestaDTO respuestaRetiro = new TransaccionRespuestaDTO(
                2L, "RETIRO", new BigDecimal("100.00"),
                new BigDecimal("900.00"), "EXITOSO", LocalDateTime.now(), "Retiro exitoso");

        when(transaccionService.retirar(any(), eq("testuser"))).thenReturn(respuestaRetiro);

        mockMvc.perform(post("/api/v1/transacciones/retirar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("RETIRO"));

        verify(transaccionService).retirar(any(), eq("testuser"));
    }

    @Test
    void transferir_retorna200YCallsService() throws Exception {
        TransferenciaSolicitudDTO dto = new TransferenciaSolicitudDTO();
        dto.setIdCuentaOrigen(1L);
        dto.setNumeroCuentaDestino("99887766");
        dto.setMonto(new BigDecimal("50.00"));

        TransaccionRespuestaDTO respuestaTransferencia = new TransaccionRespuestaDTO(
                3L, "TRANSFERENCIA", new BigDecimal("50.00"),
                new BigDecimal("950.00"), "EXITOSA", LocalDateTime.now(), "Transferencia exitosa");

        when(transaccionService.transferir(any(), eq("testuser"))).thenReturn(respuestaTransferencia);

        mockMvc.perform(post("/api/v1/transacciones/transferir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("TRANSFERENCIA"));

        verify(transaccionService).transferir(any(), eq("testuser"));
    }

    @Test
    void obtenerMovimientos_retorna200YListaVacia() throws Exception {
        when(transaccionService.obtenerMovimientos(eq(1L), eq("testuser")))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/transacciones/cuenta/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(transaccionService).obtenerMovimientos(1L, "testuser");
    }

    @Test
    void obtenerMovimientos_retornaListaConElementos() throws Exception {
        MovimientoDTO mov = new MovimientoDTO();
        mov.setConcepto("DEPOSITO");
        mov.setMonto(new BigDecimal("300.00"));

        when(transaccionService.obtenerMovimientos(eq(1L), eq("testuser")))
                .thenReturn(List.of(mov));

        mockMvc.perform(get("/api/v1/transacciones/cuenta/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].concepto").value("DEPOSITO"));
    }

    @Test
    void obtenerMovimientosPorFecha_retorna200YCallsService() throws Exception {
        when(transaccionService.obtenerMovimientosPorFecha(eq(1L), any(), any(), eq("testuser")))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/transacciones/cuenta/1/filtro")
                        .param("fechaInicio", "2026-01-01T00:00:00")
                        .param("fechaFin", "2026-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(transaccionService).obtenerMovimientosPorFecha(eq(1L), any(), any(), eq("testuser"));
    }
}
