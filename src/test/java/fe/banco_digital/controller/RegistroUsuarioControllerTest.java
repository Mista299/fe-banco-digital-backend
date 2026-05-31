package fe.banco_digital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fe.banco_digital.dto.RegistroNuevoUsuarioRequestDTO;
import fe.banco_digital.dto.RegistroNuevoUsuarioResponseDTO;
import fe.banco_digital.dto.ValidacionIdentidadResponseDTO;
import fe.banco_digital.dto.ValidarIdentidadRequestDTO;
import fe.banco_digital.entity.Genero;
import fe.banco_digital.service.RegistroUsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RegistroUsuarioControllerTest {

    @Mock RegistroUsuarioService registroUsuarioService;
    @InjectMocks RegistroUsuarioController controller;

    MockMvc mockMvc;
    ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void validarIdentidad_retornaOk() throws Exception {
        ValidacionIdentidadResponseDTO dto = new ValidacionIdentidadResponseDTO(true, "Disponible");
        when(registroUsuarioService.validarIdentidad(any())).thenReturn(dto);

        ValidarIdentidadRequestDTO req = new ValidarIdentidadRequestDTO();
        req.setDocumento("12345678");
        req.setFechaExpedicion(LocalDate.of(2020, 1, 1));

        mockMvc.perform(post("/api/v1/registro/validar-identidad")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void registrar_retornaCreated() throws Exception {
        when(registroUsuarioService.registrar(any())).thenReturn(new RegistroNuevoUsuarioResponseDTO());

        RegistroNuevoUsuarioRequestDTO dto = new RegistroNuevoUsuarioRequestDTO();
        dto.setDocumento("12345678");
        dto.setGenero(Genero.MASCULINO);
        dto.setFechaExpedicion(LocalDate.of(2020, 1, 1));
        dto.setNombre("Juan");
        dto.setEmail("juan@test.com");
        dto.setDireccion("Calle 1");
        dto.setTelefono("3001234567");
        dto.setUsername("juantest");
        dto.setPassword("Password1!");

        mockMvc.perform(post("/api/v1/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
}
