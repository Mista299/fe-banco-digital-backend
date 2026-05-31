package fe.banco_digital.controller;

import fe.banco_digital.dto.AbrirCuentaSolicitudDTO;
import fe.banco_digital.dto.CierreCuentaRespuestaDTO;
import fe.banco_digital.dto.CierreCuentaSolicitudDTO;
import fe.banco_digital.service.CuentaService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CuentaControllerTest {

    @Mock CuentaService cuentaService;
    @InjectMocks CuentaController controller;

    MockMvc mockMvc;
    UserDetails user = User.withUsername("user1").password("pw").roles("CLIENTE").build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new UserDetailsArgumentResolver(user))
                .build();
    }

    @Test
    void abrirCuenta_retornaCreated() throws Exception {
        fe.banco_digital.entity.Cuenta c = new fe.banco_digital.entity.Cuenta();
        c.setIdCuenta(1L); c.setNumeroCuenta("11112222");
        c.setTipo(fe.banco_digital.entity.TipoCuenta.AHORROS);
        c.setSaldo(java.math.BigDecimal.ZERO);
        c.setEstado(fe.banco_digital.entity.EstadoCuenta.ACTIVA);
        when(cuentaService.abrirCuenta(any(), eq("user1"))).thenReturn(new fe.banco_digital.dto.CuentaResumenDTO(c));

        mockMvc.perform(post("/api/v1/cuentas/abrir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipoCuenta\":\"AHORROS\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void cerrarCuenta_retornaOk() throws Exception {
        CierreCuentaRespuestaDTO dto = new CierreCuentaRespuestaDTO("11112222", "CERRADA", "ok");
        when(cuentaService.cerrarCuenta(any(), eq("user1"))).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/cuentas/cerrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idCuenta\":1,\"contrasena\":\"pw\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void obtenerDashboard_retornaOk() throws Exception {
        when(cuentaService.obtenerCuentasDelCliente("user1")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/cuentas/mis-cuentas"))
                .andExpect(status().isOk());
    }
}
