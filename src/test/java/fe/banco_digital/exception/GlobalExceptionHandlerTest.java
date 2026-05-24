package fe.banco_digital.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

@SuppressWarnings({"java:S1192", "java:S112"})
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

    @Test
    void manejarSaldoPendiente() throws Exception {
        mockMvc.perform(get("/test/saldo-pendiente"))
                .andExpect(status().isConflict());
    }

    @Test
    void manejarSaldoInsuficiente() throws Exception {
        mockMvc.perform(get("/test/saldo-insuficiente"))
                .andExpect(status().isConflict());
    }

    @Test
    void manejarCuentaBloqueada() throws Exception {
        mockMvc.perform(get("/test/cuenta-bloqueada"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void manejarAutenticacionFallida() throws Exception {
        mockMvc.perform(get("/test/autenticacion-fallida"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void manejarCuentaNoEncontrada() throws Exception {
        mockMvc.perform(get("/test/cuenta-no-encontrada"))
                .andExpect(status().isNotFound());
    }

    @Test
    void manejarCuentaYaCerrada() throws Exception {
        mockMvc.perform(get("/test/cuenta-ya-cerrada"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void manejarIdentificacionDuplicada() throws Exception {
        mockMvc.perform(get("/test/identificacion-duplicada"))
                .andExpect(status().isConflict());
    }

    @Test
    void manejarEmailYaExiste() throws Exception {
        mockMvc.perform(get("/test/email-ya-existe"))
                .andExpect(status().isConflict());
    }

    @Test
    void manejarAccesoNoAutorizado() throws Exception {
        mockMvc.perform(get("/test/acceso-no-autorizado"))
                .andExpect(status().isForbidden());
    }

    @Test
    void manejarCamposObligatorios() throws Exception {
        mockMvc.perform(get("/test/campos-obligatorios"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void manejarConflictoConcurrencia() throws Exception {
        mockMvc.perform(get("/test/concurrencia"))
                .andExpect(status().isConflict());
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

        @GetMapping("/test/saldo-pendiente")
        public void saldoPendiente() {
            throw new SaldoPendienteException();
        }

        @GetMapping("/test/saldo-insuficiente")
        public void saldoInsuficiente() {
            throw new SaldoInsuficienteException();
        }

        @GetMapping("/test/cuenta-bloqueada")
        public void cuentaBloqueada() {
            throw new CuentaBloqueadaException("00010001");
        }

        @GetMapping("/test/autenticacion-fallida")
        public void autenticacionFallida() {
            throw new AutenticacionFallidaException();
        }

        @GetMapping("/test/cuenta-no-encontrada")
        public void cuentaNoEncontrada() {
            throw new CuentaNoEncontradaException(1L);
        }

        @GetMapping("/test/cuenta-ya-cerrada")
        public void cuentaYaCerrada() {
            throw new CuentaYaCerradaException("00010001");
        }

        @GetMapping("/test/identificacion-duplicada")
        public void identificacionDuplicada() {
            throw new IdentificacionDuplicadaException("12345678");
        }

        @GetMapping("/test/email-ya-existe")
        public void emailYaExiste() {
            throw new EmailYaExisteException("test@test.com");
        }

        @GetMapping("/test/acceso-no-autorizado")
        public void accesoNoAutorizado() {
            throw new AccesoNoAutorizadoException();
        }

        @GetMapping("/test/campos-obligatorios")
        public void camposObligatorios() {
            throw new CamposObligatoriosException();
        }

        @GetMapping("/test/concurrencia")
        public void concurrencia() {
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException("Cuenta", 1L);
        }
    }
}
