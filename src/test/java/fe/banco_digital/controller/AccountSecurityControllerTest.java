package fe.banco_digital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fe.banco_digital.dto.SolicitudBloqueoDTO;
import fe.banco_digital.service.AccountSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountSecurityControllerTest {

    @Mock
    AccountSecurityService service;

    @InjectMocks
    AccountSecurityController controller;

    MockMvc mockMvc;
    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void bloquearCuenta_callsService() throws Exception {
        SolicitudBloqueoDTO dto = new SolicitudBloqueoDTO(); dto.setIdUsuario(1L); dto.setPassword("p");

        mockMvc.perform(post("/api/v1/cuentas/seguridad/bloquear")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(service).bloquearCuenta(1L, "p");
    }

    @Test
    void desbloquearCuenta_callsService() throws Exception {
        SolicitudBloqueoDTO dto = new SolicitudBloqueoDTO(); dto.setIdUsuario(2L); dto.setPassword("x");

        mockMvc.perform(post("/api/v1/cuentas/seguridad/desbloquear")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(service).desbloquearCuenta(2L, "x");
    }
}
