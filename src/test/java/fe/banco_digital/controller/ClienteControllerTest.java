package fe.banco_digital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fe.banco_digital.dto.ActualizarClienteDTO;
import fe.banco_digital.exception.GlobalExceptionHandler;
import fe.banco_digital.service.ClienteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClienteControllerTest {

    private MockMvc mockMvc;
    private ClienteService clienteService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        User userDetails = new User("testuser", "pass", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        clienteService = Mockito.mock(ClienteService.class);
        ClienteController controller = new ClienteController(clienteService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void actualizar_returnsOk_onValidRequest() throws Exception {
        ActualizarClienteDTO dto = new ActualizarClienteDTO();
        dto.setEmail("valid@example.com");
        dto.setTelefono("3001234567");

        doNothing().when(clienteService).actualizar(Mockito.eq(1L), Mockito.any(ActualizarClienteDTO.class), Mockito.eq("testuser"));

        mockMvc.perform(put("/api/v1/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Tus datos se han actualizado correctamente"));
    }

    @Test
    void actualizar_returnsBadRequest_onInvalidEmail() throws Exception {
        ActualizarClienteDTO dto = new ActualizarClienteDTO();
        dto.setEmail("not-an-email");
        dto.setTelefono("3001234567");

        mockMvc.perform(put("/api/v1/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }
}
