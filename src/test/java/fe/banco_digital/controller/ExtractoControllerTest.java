package fe.banco_digital.controller;

import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.GlobalExceptionHandler;
import fe.banco_digital.exception.PeriodoInvalidoException;
import fe.banco_digital.exception.PeriodoNoDisponibleException;
import fe.banco_digital.service.ExtractoService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ExtractoControllerTest {

    @Mock ExtractoService extractoService;
    @InjectMocks ExtractoController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        User userDetails = new User("testuser", "pass", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void extracto_mesCerradoConMovimientos_retorna200ConPdf() throws Exception {
        byte[] pdfMock = "%PDF-1.4 test content".getBytes();
        when(extractoService.generarExtracto(anyLong(), anyInt(), anyInt(), anyString()))
                .thenReturn(pdfMock);

        mockMvc.perform(get("/api/v1/extractos/1/2026/4"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"extracto_2026_04.pdf\""));
    }

    @Test
    void extracto_mesCerradoSinMovimientos_retorna200ConPdf() throws Exception {
        byte[] pdfVacio = "%PDF-1.4 sin movimientos".getBytes();
        when(extractoService.generarExtracto(anyLong(), anyInt(), anyInt(), anyString()))
                .thenReturn(pdfVacio);

        mockMvc.perform(get("/api/v1/extractos/1/2025/12"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void extracto_mesEnCurso_retorna422ConMensaje() throws Exception {
        when(extractoService.generarExtracto(anyLong(), anyInt(), anyInt(), anyString()))
                .thenThrow(new PeriodoNoDisponibleException());

        mockMvc.perform(get("/api/v1/extractos/1/2026/5"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value(
                        "El extracto oficial estará disponible al finalizar el periodo actual"));
    }

    @Test
    void extracto_cuentaNoPertenece_retorna403() throws Exception {
        when(extractoService.generarExtracto(anyLong(), anyInt(), anyInt(), anyString()))
                .thenThrow(new AccesoNoAutorizadoException());

        mockMvc.perform(get("/api/v1/extractos/99/2026/4"))
                .andExpect(status().isForbidden());
    }

    @Test
    void extracto_idCuentaNoNumerico_retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/extractos/abc/2026/4"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void extracto_parametrosInvalidos_retorna400() throws Exception {
        when(extractoService.generarExtracto(anyLong(), anyInt(), anyInt(), anyString()))
                .thenThrow(new PeriodoInvalidoException("El mes debe estar entre 1 y 12"));

        mockMvc.perform(get("/api/v1/extractos/1/2026/13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El mes debe estar entre 1 y 12"));
    }
}
