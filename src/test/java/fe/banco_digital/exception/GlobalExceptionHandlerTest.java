package fe.banco_digital.exception;

import fe.banco_digital.dto.RegistroRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // register a tiny controller that throws the different exceptions
        TestController controller = new TestController();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void manejarClienteNoEncontrado() throws Exception {
        MockHttpServletResponse resp = mockMvc.perform(get("/test/cliente-no-existe"))
                .andExpect(status().isNotFound())
                .andReturn().getResponse();

        assertThat(resp.getContentAsString()).contains("mensaje");
    }

    @Test
    void manejarCredencialesInvalidas() throws Exception {
        mockMvc.perform(get("/test/credenciales"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("mensaje")));
    }

    @Test
    void manejarUsuarioYaExisteYClienteTieneUsuario() throws Exception {
        mockMvc.perform(get("/test/usuario-ya-existe"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/test/cliente-ya-tiene-usuario"))
                .andExpect(status().isConflict());
    }

    @Test
    void manejarTokenInvalidoYExpirado() throws Exception {
        mockMvc.perform(get("/test/token-invalido"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/test/token-expirado"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void manejarValidacion() throws Exception {
        mockMvc.perform(get("/test/validacion"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("mensaje")));
    }

    @Test
    void manejarErrorGenerico() throws Exception {
        mockMvc.perform(get("/test/generico"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No se pudo cargar")));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/cliente-no-existe")
        public void notFound() {
            throw new ClienteNoEncontradoException(1L);
        }

        @GetMapping("/test/credenciales")
        public void creds() {
            throw new CredencialesInvalidasException();
        }

        @GetMapping("/test/usuario-ya-existe")
        public void usuarioExiste() {
            throw new UsuarioYaExisteException("dup");
        }

        @GetMapping("/test/cliente-ya-tiene-usuario")
        public void clienteTieneUsuario() {
            throw new ClienteYaTieneUsuarioException(2L);
        }

        @GetMapping("/test/token-invalido")
        public void tokenInv() {
            throw new TokenInvalidoException();
        }

        @GetMapping("/test/token-expirado")
        public void tokenExp() {
            throw new TokenExpiradoException();
        }

        @GetMapping("/test/validacion")
        public void validacion() throws Exception {
            // construct a MethodArgumentNotValidException with a FieldError
            Method method = TestController.class.getMethod("validacion");
            org.springframework.core.MethodParameter mp = new org.springframework.core.MethodParameter(method, -1);
            BeanPropertyBindingResult br = new BeanPropertyBindingResult(new Object(), "obj");
            br.addError(new FieldError("obj", "campo", "valor inválido"));
            throw new MethodArgumentNotValidException(mp, br);
        }

        @GetMapping("/test/generico")
        public void generico() {
            throw new RuntimeException("boom");
        }
    }
}
