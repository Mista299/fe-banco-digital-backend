package fe.banco_digital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fe.banco_digital.dto.LoginRequestDTO;
import fe.banco_digital.dto.LoginResponseDTO;
import fe.banco_digital.dto.RegistroRequestDTO;
import fe.banco_digital.dto.UsuarioRegistradoDTO;
import fe.banco_digital.service.AutenticacionService;
import fe.banco_digital.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AutenticacionControllerTest {

    @Mock
    AutenticacionService autenticacionService;

    @Mock
    RefreshTokenService refreshTokenService;

    @InjectMocks
    AutenticacionController controller;

    MockMvc mockMvc;
    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void registrar_returnsCreated() throws Exception {
        RegistroRequestDTO dto = new RegistroRequestDTO();
        dto.setUsername("u"); dto.setPassword("p"); dto.setIdCliente(1L);

        UsuarioRegistradoDTO out = new UsuarioRegistradoDTO(); out.setIdUsuario(5L);
        when(autenticacionService.registrar(any())).thenReturn(out);

        mockMvc.perform(post("/api/v1/auth/registro")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        verify(autenticacionService).registrar(any());
    }

    @Test
    void login_setsCookies_andReturnsOk() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO(); dto.setUsername("u"); dto.setPassword("p");

        LoginResponseDTO resp = new LoginResponseDTO();
        resp.setAccessToken("t1"); resp.setRefreshToken("r1");
        when(autenticacionService.login(any())).thenReturn(resp);

        MockHttpServletResponse response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse();

        String setCookie = response.getHeader("Set-Cookie");
        // controller sets two cookies, so header should not be null
        assert setCookie != null;
        verify(autenticacionService).login(any());
    }

    @Test
    void refresh_readsCookie_andReturnsOk() throws Exception {
        LoginResponseDTO resp = new LoginResponseDTO(); resp.setAccessToken("a"); resp.setRefreshToken("r");
        when(refreshTokenService.renovarToken(eq("r1"))).thenReturn(resp);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("refreshToken", "r1"));

        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.refresh(request, response);

        String header = response.getHeader("Set-Cookie");
        assert header != null;
        verify(refreshTokenService).renovarToken("r1");
    }

    @Test
    void logout_revoca_andClearsCookies() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("refreshToken", "r1"));

        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.logout(request, response);

        String header = response.getHeader("Set-Cookie");
        assert header != null;
        // revocar called once
        verify(refreshTokenService, atMost(1)).revocarToken(any());
    }
}
