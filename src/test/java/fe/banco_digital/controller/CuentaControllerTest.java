package fe.banco_digital.controller;

import fe.banco_digital.dto.CuentaResumenDTO;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.TipoCuenta;
import fe.banco_digital.service.CuentaService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CuentaControllerTest {

    @Mock
    CuentaService cuentaService;

    @InjectMocks
    CuentaController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        User userDetails = new User("testuser", "pass", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Cuenta cuenta(Long id, String numero, TipoCuenta tipo, BigDecimal saldo, EstadoCuenta estado) {
        Cuenta c = new Cuenta();
        c.setIdCuenta(id);
        c.setNumeroCuenta(numero);
        c.setTipo(tipo);
        c.setSaldo(saldo);
        c.setEstado(estado);
        return c;
    }

    // Escenario 1: múltiples cuentas activas — número enmascarado y saldoDisponible
    @Test
    void obtenerDashboard_retornaListaEnmascarada_cuandoClienteTieneVariasCuentas() throws Exception {
        List<CuentaResumenDTO> dtos = List.of(
                new CuentaResumenDTO(cuenta(1L, "00010001", TipoCuenta.AHORROS,   new BigDecimal("150000"), EstadoCuenta.ACTIVA)),
                new CuentaResumenDTO(cuenta(2L, "00020002", TipoCuenta.CORRIENTE, new BigDecimal("75000"),  EstadoCuenta.ACTIVA))
        );
        when(cuentaService.obtenerCuentasDelCliente("testuser")).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/cuentas/dashboard").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].numeroEnmascarado").value("****0001"))
                .andExpect(jsonPath("$[0].saldoDisponible").value(true))
                .andExpect(jsonPath("$[0].permiteTransacciones").value(true))
                .andExpect(jsonPath("$[1].numeroEnmascarado").value("****0002"))
                .andExpect(jsonPath("$[1].tipo").value("CORRIENTE"));
    }

    // Escenario 2: un único producto — lista con un elemento
    @Test
    void obtenerDashboard_retornaUnElemento_cuandoClienteTieneUnaCuenta() throws Exception {
        List<CuentaResumenDTO> dtos = List.of(
                new CuentaResumenDTO(cuenta(1L, "00010001", TipoCuenta.AHORROS, new BigDecimal("200000"), EstadoCuenta.ACTIVA))
        );
        when(cuentaService.obtenerCuentasDelCliente("testuser")).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/cuentas/dashboard").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].numeroEnmascarado").value("****0001"))
                .andExpect(jsonPath("$[0].saldo").value(200000));
    }

    // Escenario 3: cuenta inactiva — etiqueta visual y transacciones bloqueadas
    @Test
    void obtenerDashboard_marcaCuentaCerrada_cuandoEstadoEsInactiva() throws Exception {
        List<CuentaResumenDTO> dtos = List.of(
                new CuentaResumenDTO(cuenta(1L, "00010001", TipoCuenta.AHORROS, new BigDecimal("150000"), EstadoCuenta.ACTIVA)),
                new CuentaResumenDTO(cuenta(2L, "00050001", TipoCuenta.AHORROS, BigDecimal.ZERO,          EstadoCuenta.INACTIVA))
        );
        when(cuentaService.obtenerCuentasDelCliente("testuser")).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/cuentas/dashboard").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].etiquetaVisual").value("Cuenta Cerrada"))
                .andExpect(jsonPath("$[1].permiteTransacciones").value(false))
                .andExpect(jsonPath("$[1].saldoDisponible").value(true));
    }

    // Lista vacía cuando el cliente no tiene cuentas
    @Test
    void obtenerDashboard_retornaListaVacia_cuandoClienteSinCuentas() throws Exception {
        when(cuentaService.obtenerCuentasDelCliente("testuser")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/cuentas/dashboard").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
