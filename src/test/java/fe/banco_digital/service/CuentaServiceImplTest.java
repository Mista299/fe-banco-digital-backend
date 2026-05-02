package fe.banco_digital.service;

import fe.banco_digital.dto.CuentaResumenDTO;
import fe.banco_digital.entity.*;
import fe.banco_digital.exception.AutenticacionFallidaException;
import fe.banco_digital.mapper.CuentaMapper;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    @Mock
    CuentaRepository cuentaRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Spy
    CuentaMapper cuentaMapper;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    CuentaServiceImpl cuentaService;

    private Usuario usuarioConCliente(Long idCliente) {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(idCliente);

        Usuario usuario = new Usuario();
        usuario.setUsername("testuser");
        usuario.setCliente(cliente);
        return usuario;
    }

    private Cuenta cuentaActiva(Long id, String numero, BigDecimal saldo) {
        Cuenta c = new Cuenta();
        c.setIdCuenta(id);
        c.setNumeroCuenta(numero);
        c.setTipo(TipoCuenta.AHORROS);
        c.setSaldo(saldo);
        c.setEstado(EstadoCuenta.ACTIVA);
        return c;
    }

    // Escenario 1: múltiples cuentas → lista con números enmascarados
    @Test
    void obtenerCuentasDelCliente_retornaListaEnmascarada_cuandoTieneVariasCuentas() {
        Usuario usuario = usuarioConCliente(1L);
        List<Cuenta> cuentas = List.of(
                cuentaActiva(1L, "00010001", new BigDecimal("150000")),
                cuentaActiva(2L, "00020002", new BigDecimal("75000"))
        );

        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByCliente_IdCliente(1L)).thenReturn(cuentas);

        List<CuentaResumenDTO> resultado = cuentaService.obtenerCuentasDelCliente("testuser");

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNumeroEnmascarado()).isEqualTo("****0001");
        assertThat(resultado.get(0).isSaldoDisponible()).isTrue();
        assertThat(resultado.get(0).isPermiteTransacciones()).isTrue();
        assertThat(resultado.get(1).getNumeroEnmascarado()).isEqualTo("****0002");
    }

    // Escenario 2: una sola cuenta → lista con un elemento
    @Test
    void obtenerCuentasDelCliente_retornaUnElemento_cuandoTieneUnaCuenta() {
        Usuario usuario = usuarioConCliente(2L);
        List<Cuenta> cuentas = List.of(cuentaActiva(1L, "00010001", new BigDecimal("200000")));

        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByCliente_IdCliente(2L)).thenReturn(cuentas);

        List<CuentaResumenDTO> resultado = cuentaService.obtenerCuentasDelCliente("testuser");

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getSaldo()).isEqualByComparingTo("200000");
    }

    // Escenario 3: cuenta inactiva → etiqueta y transacciones bloqueadas
    @Test
    void obtenerCuentasDelCliente_marcaCuentaCerrada_cuandoEstadoEsInactiva() {
        Usuario usuario = usuarioConCliente(3L);
        Cuenta cerrada = new Cuenta();
        cerrada.setIdCuenta(2L);
        cerrada.setNumeroCuenta("00050001");
        cerrada.setTipo(TipoCuenta.AHORROS);
        cerrada.setSaldo(BigDecimal.ZERO);
        cerrada.setEstado(EstadoCuenta.INACTIVA);

        when(usuarioRepository.findByUsername("testuser")).thenReturn(Optional.of(usuario));
        when(cuentaRepository.findByCliente_IdCliente(3L)).thenReturn(List.of(cerrada));

        List<CuentaResumenDTO> resultado = cuentaService.obtenerCuentasDelCliente("testuser");

        assertThat(resultado.get(0).getEtiquetaVisual()).isEqualTo("Cuenta Cerrada");
        assertThat(resultado.get(0).isPermiteTransacciones()).isFalse();
    }

    // Usuario no existe → lanza excepción de autenticación
    @Test
    void obtenerCuentasDelCliente_lanzaExcepcion_cuandoUsuarioNoExiste() {
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cuentaService.obtenerCuentasDelCliente("fantasma"))
                .isInstanceOf(AutenticacionFallidaException.class);
    }
}
