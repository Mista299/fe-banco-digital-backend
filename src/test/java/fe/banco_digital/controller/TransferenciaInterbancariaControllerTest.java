package fe.banco_digital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fe.banco_digital.dto.ConfirmacionAchSolicitudDTO;
import fe.banco_digital.dto.RechazoAchSolicitudDTO;
import fe.banco_digital.dto.TransferenciaInterbancariaResponseDTO;
import fe.banco_digital.dto.TransferenciaInterbancariaSolicitudDTO;
import fe.banco_digital.exception.GlobalExceptionHandler;
import fe.banco_digital.service.TransferenciaInterbancariaService;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class TransferenciaInterbancariaControllerTest {

    static final String GATEWAY_SECRET = "test-gateway-secret";

    @Mock TransferenciaInterbancariaService service;

    @InjectMocks TransferenciaInterbancariaController controller;

    MockMvc mockMvc;
    ObjectMapper mapper = new ObjectMapper();

    TransferenciaInterbancariaResponseDTO respuestaBase;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "gatewaySecret", GATEWAY_SECRET);

        User userDetails = new User("testuser", "pass", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        respuestaBase = new TransferenciaInterbancariaResponseDTO(
                1L, "ACH-TEST-001", new BigDecimal("100000.00"),
                new BigDecimal("400000.00"), "PENDIENTE_PROCESAMIENTO",
                LocalDateTime.now(), "Transferencia ACH enviada");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private TransferenciaInterbancariaSolicitudDTO solicitudValida() {
        TransferenciaInterbancariaSolicitudDTO dto = new TransferenciaInterbancariaSolicitudDTO();
        dto.setIdCuentaOrigen(1L);
        dto.setBancoDestino("Bancolombia");
        dto.setTipoCuentaDestino("AHORROS");
        dto.setNumeroCuentaDestino("12345678901");
        dto.setTipoDocumentoReceptor("CC");
        dto.setNumeroDocumentoReceptor("9876543210");
        dto.setNombreReceptor("Pedro Suárez");
        dto.setMonto(new BigDecimal("100000.00"));
        return dto;
    }

    @Test
    void crear_retorna200YCallsService() throws Exception {
        when(service.iniciarTransferencia(any(), eq("testuser"))).thenReturn(respuestaBase);

        mockMvc.perform(post("/api/v1/transferencias/interbancarias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(solicitudValida())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_PROCESAMIENTO"));

        verify(service).iniciarTransferencia(any(), eq("testuser"));
    }

    @Test
    void registrarRechazoAch_secretCorrecto_retorna200() throws Exception {
        TransferenciaInterbancariaResponseDTO reversada = new TransferenciaInterbancariaResponseDTO(
                1L, "ACH-TEST-001", new BigDecimal("100000.00"),
                new BigDecimal("500000.00"), "REVERSADA",
                LocalDateTime.now(), "ACH rechazado y reversado");

        when(service.registrarRechazoAch(eq(1L), any())).thenReturn(reversada);

        RechazoAchSolicitudDTO rechazo = new RechazoAchSolicitudDTO();
        rechazo.setMotivo("Cuenta destino no existe");

        mockMvc.perform(post("/api/v1/transferencias/interbancarias/1/rechazo-ach")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .content(mapper.writeValueAsString(rechazo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REVERSADA"));

        verify(service).registrarRechazoAch(eq(1L), any());
    }

    @Test
    void registrarRechazoAch_secretIncorrecto_retorna403() throws Exception {
        RechazoAchSolicitudDTO rechazo = new RechazoAchSolicitudDTO();
        rechazo.setMotivo("motivo");

        mockMvc.perform(post("/api/v1/transferencias/interbancarias/1/rechazo-ach")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", "secreto_incorrecto")
                        .content(mapper.writeValueAsString(rechazo)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarConfirmacionAch_secretCorrecto_retorna200() throws Exception {
        TransferenciaInterbancariaResponseDTO exitosa = new TransferenciaInterbancariaResponseDTO(
                1L, "ACH-CONF-001", new BigDecimal("100000.00"),
                new BigDecimal("400000.00"), "EXITOSA",
                LocalDateTime.now(), "ACH confirmado");

        when(service.registrarConfirmacionAch(eq(1L), any())).thenReturn(exitosa);

        ConfirmacionAchSolicitudDTO confirmacion = new ConfirmacionAchSolicitudDTO();
        confirmacion.setReferenciaConfirmacion("ACH-CONF-001");

        mockMvc.perform(post("/api/v1/transferencias/interbancarias/1/confirmacion-ach")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .content(mapper.writeValueAsString(confirmacion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EXITOSA"));

        verify(service).registrarConfirmacionAch(eq(1L), any());
    }

    @Test
    void registrarConfirmacionAch_secretIncorrecto_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/transferencias/interbancarias/1/confirmacion-ach")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", "incorrecto")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void consultar_retorna200YCallsService() throws Exception {
        when(service.consultarTransferencia(eq(1L), eq("testuser"))).thenReturn(respuestaBase);

        mockMvc.perform(get("/api/v1/transferencias/interbancarias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenciaExterna").value("ACH-TEST-001"));

        verify(service).consultarTransferencia(1L, "testuser");
    }
}
