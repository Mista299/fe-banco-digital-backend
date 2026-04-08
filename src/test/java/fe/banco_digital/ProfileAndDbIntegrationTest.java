package fe.banco_digital;

import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.TipoCuenta;
import fe.banco_digital.repository.ClienteRepository;
import fe.banco_digital.repository.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class ProfileAndDbIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    @BeforeEach
    void setup() {
        cuentaRepository.deleteAll();
        clienteRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    void dbPing_returnsOk() throws Exception {
        mockMvc.perform(get("/api/db/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(1));
    }

    @Test
    void getProfile_returnsProfile() throws Exception {
        Cliente c = new Cliente();
        c.setNombre("Test User");
        c.setDocumento("99999");
        c.setEmail("test@example.com");
        clienteRepository.save(c);

        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta("5000000001");
        cuenta.setTipo(TipoCuenta.AHORROS);
        cuenta.setEstado(EstadoCuenta.ACTIVA);
        cuenta.setSaldo(new BigDecimal("1000.00"));
        cuenta.setCliente(c);
        cuentaRepository.save(cuenta);

        mockMvc.perform(get("/api/profile/" + c.getIdCliente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.identificationNumber").value("99999"))
                .andExpect(jsonPath("$.accountNumber").value("5000000001"))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }
}
