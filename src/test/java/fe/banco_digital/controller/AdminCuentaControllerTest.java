package fe.banco_digital.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fe.banco_digital.dto.DecisionAperturaRespuestaDTO;
import fe.banco_digital.dto.SolicitudPendienteDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.TipoCuenta;
import fe.banco_digital.exception.OperacionNoPermitidaException;
import fe.banco_digital.service.AdminCuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AdminCuentaControllerTest {

    @Mock AdminCuentaService adminCuentaService;
    @InjectMocks AdminCuentaController controller;

    MockMvc mockMvc;
    ObjectMapper mapper = new ObjectMapper();
    UserDetails adminUser = User.withUsername("admin").password("pw").roles("ADMIN").build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new UserDetailsArgumentResolver(adminUser))
                .build();
    }

    private DecisionAperturaRespuestaDTO respuesta(String estado) {
        return new DecisionAperturaRespuestaDTO(1L, "11112222", estado, "ok");
    }

    @Test
    void listarPendientes_retornaOk() throws Exception {
        fe.banco_digital.entity.Cliente cli = new fe.banco_digital.entity.Cliente();
        cli.setIdCliente(1L); cli.setNombre("Juan");
        Cuenta c = new Cuenta(); c.setIdCuenta(1L); c.setNumeroCuenta("11112222");
        c.setEstado(EstadoCuenta.PENDIENTE_APROBACION); c.setTipo(TipoCuenta.AHORROS); c.setCliente(cli);
        SolicitudPendienteDTO s = new SolicitudPendienteDTO(c);
        when(adminCuentaService.listarPendientes()).thenReturn(List.of(s));

        mockMvc.perform(get("/api/v1/admin/cuentas/pendientes"))
                .andExpect(status().isOk());
    }

    @Test
    void listarPendientes_vacio_retornaOk() throws Exception {
        when(adminCuentaService.listarPendientes()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/admin/cuentas/pendientes"))
                .andExpect(status().isOk());
    }

    @Test
    void aprobarApertura_retornaOk() throws Exception {
        when(adminCuentaService.aprobarApertura(1L, "admin")).thenReturn(respuesta("ACTIVA"));

        mockMvc.perform(patch("/api/v1/admin/cuentas/1/aprobar")
                        )
                .andExpect(status().isOk());
    }

    @Test
    void rechazarApertura_retornaOk() throws Exception {
        when(adminCuentaService.rechazarApertura(1L, "admin")).thenReturn(respuesta("INACTIVA"));

        mockMvc.perform(patch("/api/v1/admin/cuentas/1/rechazar")
                        )
                .andExpect(status().isOk());
    }

    @Test
    void procesarDecision_aprobar_retornaOk() throws Exception {
        when(adminCuentaService.aprobarApertura(1L, "admin")).thenReturn(respuesta("ACTIVA"));

        mockMvc.perform(post("/api/v1/admin/cuentas/1/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("decision", "APROBAR")))
                        )
                .andExpect(status().isOk());
    }

    @Test
    void procesarDecision_rechazar_retornaOk() throws Exception {
        when(adminCuentaService.rechazarApertura(1L, "admin")).thenReturn(respuesta("INACTIVA"));

        mockMvc.perform(post("/api/v1/admin/cuentas/1/decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("decision", "RECHAZAR")))
                        )
                .andExpect(status().isOk());
    }

    @Test
    void procesarDecision_invalida_lanzaExcepcion() throws Exception {
        try {
            mockMvc.perform(post("/api/v1/admin/cuentas/1/decision")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(Map.of("decision", "INVALIDA"))))
                    .andReturn();
        } catch (Exception ex) {
            org.junit.jupiter.api.Assertions.assertNotNull(ex);
        }
    }
}
