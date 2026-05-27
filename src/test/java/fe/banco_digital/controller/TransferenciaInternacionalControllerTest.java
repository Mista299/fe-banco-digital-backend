package fe.banco_digital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fe.banco_digital.dto.ConfirmacionSwiftSolicitudDTO;
import fe.banco_digital.dto.RechazoSwiftSolicitudDTO;
import fe.banco_digital.dto.TransferenciaInternacionalResponseDTO;
import fe.banco_digital.dto.TransferenciaInternacionalSolicitudDTO;
import fe.banco_digital.exception.GlobalExceptionHandler;
import fe.banco_digital.service.TransferenciaInternacionalService;
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
class TransferenciaInternacionalControllerTest {

    static final String GATEWAY_SECRET = "test-gateway-secret";

    @Mock TransferenciaInternacionalService service;

    @InjectMocks TransferenciaInternacionalController controller;

    MockMvc mockMvc;
    ObjectMapper mapper = new ObjectMapper();

    TransferenciaInternacionalResponseDTO respuestaPendiente;
    TransferenciaInternacionalResponseDTO respuestaExitosa;
    TransferenciaInternacionalResponseDTO respuestaReversada;

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

        respuestaPendiente = new TransferenciaInternacionalResponseDTO(
                1L, "SWIFT-TEST-001", new BigDecimal("100.00"),
                new BigDecimal("420000.00"), new BigDecimal("4200"),
                new BigDecimal("580000.00"), "PENDIENTE_PROCESAMIENTO",
                LocalDateTime.now(), "Orden SWIFT enviada");

        respuestaExitosa = new TransferenciaInternacionalResponseDTO(
                1L, "SWIFT-CONF-001", new BigDecimal("100.00"),
                new BigDecimal("420000.00"), new BigDecimal("4200"),
                new BigDecimal("580000.00"), "EXITOSA",
                LocalDateTime.now(), "SWIFT confirmado");

        respuestaReversada = new TransferenciaInternacionalResponseDTO(
                1L, "SWIFT-TEST-001", new BigDecimal("100.00"),
                new BigDecimal("420000.00"), new BigDecimal("4200"),
                new BigDecimal("1000000.00"), "REVERSADA",
                LocalDateTime.now(), "SWIFT rechazado y reversado");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private TransferenciaInternacionalSolicitudDTO solicitudValida() {
        TransferenciaInternacionalSolicitudDTO dto = new TransferenciaInternacionalSolicitudDTO();
        dto.setIdCuentaOrigen(1L);
        dto.setBancoDestino("Citibank");
        dto.setCodigoSwift("CITIUS33");
        dto.setPaisDestino("Estados Unidos");
        dto.setTipoCuentaDestino("CHECKING");
        dto.setIbanCuentaDestino("US64SVBK0000000000000000");
        dto.setTipoDocumentoReceptor("PASSPORT");
        dto.setNumeroDocumentoReceptor("A123456");
        dto.setNombreReceptor("John Smith");
        dto.setMontoUsd(new BigDecimal("100.00"));
        dto.setTasaCambio(new BigDecimal("4200"));
        dto.setMoneda("USD");
        return dto;
    }

    @Test
    void crear_retorna200YEstadoPendiente() throws Exception {
        when(service.iniciarTransferencia(any(), eq("testuser"))).thenReturn(respuestaPendiente);

        mockMvc.perform(post("/api/v1/transferencias/internacionales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(solicitudValida())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDIENTE_PROCESAMIENTO"))
                .andExpect(jsonPath("$.referenciaSwift").value("SWIFT-TEST-001"));

        verify(service).iniciarTransferencia(any(), eq("testuser"));
    }

    @Test
    void registrarConfirmacionSwift_secretCorrecto_retorna200() throws Exception {
        when(service.registrarConfirmacionSwift(eq(1L), any())).thenReturn(respuestaExitosa);

        ConfirmacionSwiftSolicitudDTO confirmacion = new ConfirmacionSwiftSolicitudDTO();
        confirmacion.setReferenciaConfirmacion("SWIFT-CONF-001");

        mockMvc.perform(post("/api/v1/transferencias/internacionales/1/confirmacion-swift")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .content(mapper.writeValueAsString(confirmacion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EXITOSA"));

        verify(service).registrarConfirmacionSwift(eq(1L), any());
    }

    @Test
    void registrarConfirmacionSwift_secretIncorrecto_retorna403() throws Exception {
        mockMvc.perform(post("/api/v1/transferencias/internacionales/1/confirmacion-swift")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", "secreto_malo")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarRechazoSwift_secretCorrecto_retorna200() throws Exception {
        when(service.registrarRechazoSwift(eq(1L), any())).thenReturn(respuestaReversada);

        RechazoSwiftSolicitudDTO rechazo = new RechazoSwiftSolicitudDTO();
        rechazo.setMotivo("Código SWIFT inválido");

        mockMvc.perform(post("/api/v1/transferencias/internacionales/1/rechazo-swift")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", GATEWAY_SECRET)
                        .content(mapper.writeValueAsString(rechazo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REVERSADA"));

        verify(service).registrarRechazoSwift(eq(1L), any());
    }

    @Test
    void registrarRechazoSwift_secretIncorrecto_retorna403() throws Exception {
        RechazoSwiftSolicitudDTO rechazo = new RechazoSwiftSolicitudDTO();
        rechazo.setMotivo("motivo");

        mockMvc.perform(post("/api/v1/transferencias/internacionales/1/rechazo-swift")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Gateway-Secret", "incorrecto")
                        .content(mapper.writeValueAsString(rechazo)))
                .andExpect(status().isForbidden());
    }

    @Test
    void consultar_retorna200YCallsService() throws Exception {
        when(service.consultarTransferencia(eq(1L), eq("testuser"))).thenReturn(respuestaPendiente);

        mockMvc.perform(get("/api/v1/transferencias/internacionales/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referenciaSwift").value("SWIFT-TEST-001"));

        verify(service).consultarTransferencia(1L, "testuser");
    }
}
