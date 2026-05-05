package fe.banco_digital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fe.banco_digital.dto.ComprobanteDepositoDTO;
import fe.banco_digital.dto.NotificacionDepositoDTO;
import fe.banco_digital.dto.RechazoDepositoDTO;
import fe.banco_digital.exception.DepositoRechazadoException;
import fe.banco_digital.exception.GlobalExceptionHandler;
import fe.banco_digital.security.FiltroHmacGateway;
import fe.banco_digital.service.DepositoGatewayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DepositoGatewayControllerTest {

    private static final String SECRETO_PRUEBA = "secreto-test-hmac";

    private MockMvc mockMvc;
    private DepositoGatewayService depositoGatewayService;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        depositoGatewayService = Mockito.mock(DepositoGatewayService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DepositoGatewayController(depositoGatewayService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilter(new FiltroHmacGateway(SECRETO_PRUEBA))
                .build();
    }

    @Test
    void recibirNotificacion_retorna200ConComprobante_cuandoEsExitoso() throws Exception {
        ComprobanteDepositoDTO comprobante = new ComprobanteDepositoDTO(
                1L, LocalDateTime.now(), new BigDecimal("50000.00"),
                "5001000001", new BigDecimal("150000.00"), "EXITOSA");

        when(depositoGatewayService.procesarNotificacion(any())).thenReturn(comprobante);

        String cuerpo = """
                {
                  "numeroCuenta": "5001000001",
                  "monto": 50000.00,
                  "referenciaGateway": "REF-001",
                  "canalOrigen": "PSE"
                }
                """;

        mockMvc.perform(post("/api/v1/depositos/notificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Signature", calcularFirma(cuerpo))
                        .content(cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroOperacion").value(1))
                .andExpect(jsonPath("$.numeroCuentaDestino").value("5001000001"))
                .andExpect(jsonPath("$.estado").value("EXITOSA"))
                .andExpect(jsonPath("$.saldoResultante").value(150000.00));
    }

    @Test
    void recibirNotificacion_retorna422ConRechazo_cuandoCuentaBloqueada() throws Exception {
        RechazoDepositoDTO rechazo = new RechazoDepositoDTO(
                "Cuenta bloqueada.", "5001000001",
                new BigDecimal("50000.00"), "REF-001", "PSE");

        when(depositoGatewayService.procesarNotificacion(any()))
                .thenThrow(new DepositoRechazadoException(rechazo));

        String cuerpo = """
                {
                  "numeroCuenta": "5001000001",
                  "monto": 50000.00,
                  "referenciaGateway": "REF-001",
                  "canalOrigen": "PSE"
                }
                """;

        mockMvc.perform(post("/api/v1/depositos/notificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Signature", calcularFirma(cuerpo))
                        .content(cuerpo))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.motivo").value("Cuenta bloqueada."))
                .andExpect(jsonPath("$.devolucionSimulada").value(true))
                .andExpect(jsonPath("$.mensajeDevolucion").exists());
    }

    @Test
    void recibirNotificacion_retorna400_cuandoFaltanCampos() throws Exception {
        String cuerpo = """
                {
                  "numeroCuenta": "5001000001"
                }
                """;

        mockMvc.perform(post("/api/v1/depositos/notificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Signature", calcularFirma(cuerpo))
                        .content(cuerpo))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recibirNotificacion_retorna401_cuandoFaltaFirma() throws Exception {
        mockMvc.perform(post("/api/v1/depositos/notificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numeroCuenta": "5001000001",
                                  "monto": 50000.00,
                                  "referenciaGateway": "REF-001",
                                  "canalOrigen": "PSE"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void recibirNotificacion_retorna401_cuandoFirmaEsInvalida() throws Exception {
        mockMvc.perform(post("/api/v1/depositos/notificacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Signature", "sha256=firmainvalida000")
                        .content("""
                                {
                                  "numeroCuenta": "5001000001",
                                  "monto": 50000.00,
                                  "referenciaGateway": "REF-001",
                                  "canalOrigen": "PSE"
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    private String calcularFirma(String cuerpo) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRETO_PRUEBA.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] firma = mac.doFinal(cuerpo.getBytes(StandardCharsets.UTF_8));
        return "sha256=" + HexFormat.of().formatHex(firma);
    }
}
