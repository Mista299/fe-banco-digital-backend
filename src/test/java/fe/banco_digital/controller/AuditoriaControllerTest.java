package fe.banco_digital.controller;

import fe.banco_digital.dto.AuditoriaDTO;
import fe.banco_digital.service.AuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuditoriaControllerTest {

    @Mock AuditoriaService auditoriaService;
    @InjectMocks AuditoriaController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void obtenerTodos_retornaOk() throws Exception {
        when(auditoriaService.obtenerTodos()).thenReturn(List.of(
                new AuditoriaDTO(1L, "LOGIN", "user1", "detalle", null, "INFO")));
        mockMvc.perform(get("/api/v1/admin/auditoria"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerTodos_vacio_retornaOk() throws Exception {
        when(auditoriaService.obtenerTodos()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/v1/admin/auditoria"))
                .andExpect(status().isOk());
    }
}
