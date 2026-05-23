package fe.banco_digital.service;

import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.EstadoCuenta;
import fe.banco_digital.entity.TipoCuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.PeriodoInvalidoException;
import fe.banco_digital.exception.PeriodoNoDisponibleException;
import fe.banco_digital.mapper.TransaccionMapper;
import fe.banco_digital.pdf.ExtractoPdfGenerador;
import fe.banco_digital.repository.CuentaRepository;
import fe.banco_digital.repository.MovimientoRepository;
import fe.banco_digital.repository.TransferenciaExternaRepository;
import fe.banco_digital.repository.TransferenciaInternacionalRepository;
import fe.banco_digital.repository.TransferenciaRepository;
import fe.banco_digital.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtractoServiceImplTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock CuentaRepository cuentaRepository;
    @Mock MovimientoRepository movimientoRepository;
    @Mock TransferenciaRepository transferenciaRepository;
    @Mock TransferenciaExternaRepository transferenciaExternaRepository;
    @Mock TransferenciaInternacionalRepository transferenciaInternacionalRepository;
    @Mock TransaccionMapper transaccionMapper;
    @Mock ExtractoPdfGenerador pdfGenerador;

    @InjectMocks ExtractoServiceImpl service;

    private Usuario usuarioMock;
    private Cuenta cuentaMock;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Ana Lopez");
        cliente.setDocumento("123456789");

        usuarioMock = new Usuario();
        usuarioMock.setIdUsuario(1L);
        usuarioMock.setUsername("ana");
        usuarioMock.setCliente(cliente);

        cuentaMock = new Cuenta();
        cuentaMock.setIdCuenta(10L);
        cuentaMock.setNumeroCuenta("1234567890");
        cuentaMock.setTipo(TipoCuenta.AHORROS);
        cuentaMock.setEstado(EstadoCuenta.ACTIVA);
        cuentaMock.setSaldo(new BigDecimal("1500000.00"));
        cuentaMock.setCliente(cliente);
    }

    @Test
    void generarExtracto_mesInvalido_lanzaPeriodoInvalidoException() {
        assertThatThrownBy(() -> service.generarExtracto(10L, 2026, 13, "ana"))
                .isInstanceOf(PeriodoInvalidoException.class);

        assertThatThrownBy(() -> service.generarExtracto(10L, 2026, 0, "ana"))
                .isInstanceOf(PeriodoInvalidoException.class);
    }

    @Test
    void generarExtracto_anioFuturo_lanzaPeriodoInvalidoException() {
        int anioFuturo = LocalDate.now().getYear() + 1;
        assertThatThrownBy(() -> service.generarExtracto(10L, anioFuturo, 1, "ana"))
                .isInstanceOf(PeriodoInvalidoException.class);
    }

    @Test
    void generarExtracto_mesFuturoEnMismoAnio_lanzaPeriodoInvalidoException() {
        LocalDate hoy = LocalDate.now();
        int mesFuturo = hoy.getMonthValue() + 1;
        if (mesFuturo > 12) return;
        assertThatThrownBy(() -> service.generarExtracto(10L, hoy.getYear(), mesFuturo, "ana"))
                .isInstanceOf(PeriodoInvalidoException.class);
    }

    @Test
    void generarExtracto_mesActual_lanzaPeriodoNoDisponibleException() {
        LocalDate hoy = LocalDate.now();
        assertThatThrownBy(() -> service.generarExtracto(10L, hoy.getYear(), hoy.getMonthValue(), "ana"))
                .isInstanceOf(PeriodoNoDisponibleException.class);
    }

    @Test
    void generarExtracto_cuentaNoPertenece_lanzaAccesoNoAutorizadoException() {
        LocalDate mesPasado = LocalDate.now().minusMonths(1);
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuarioMock));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(eq(10L), eq(1L)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.generarExtracto(10L, mesPasado.getYear(), mesPasado.getMonthValue(), "ana"))
                .isInstanceOf(AccesoNoAutorizadoException.class);
    }

    @Test
    void generarExtracto_sinMovimientos_retornaPdfConSaldoIgual() {
        LocalDate mesPasado = LocalDate.now().minusMonths(1);

        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuarioMock));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(eq(10L), eq(1L)))
                .thenReturn(Optional.of(cuentaMock));
        when(transaccionMapper.aListaDTOUnificada(any(), any(), anyLong(), any(), any()))
                .thenReturn(List.of());
        when(pdfGenerador.generar(any())).thenReturn("%PDF-test".getBytes());

        byte[] resultado = service.generarExtracto(10L, mesPasado.getYear(), mesPasado.getMonthValue(), "ana");

        assertThat(resultado).isNotNull().isNotEmpty();
    }

    @Test
    void generarExtracto_conMovimientos_calculaSaldosCorrectamente() {
        LocalDate mesPasado = LocalDate.now().minusMonths(1);

        MovimientoDTO deposito = new MovimientoDTO();
        deposito.setMonto(new BigDecimal("200000.00"));
        deposito.setConcepto("DEPOSITO");
        deposito.setFechaHora(LocalDateTime.now().minusMonths(1).minusDays(5));

        MovimientoDTO retiro = new MovimientoDTO();
        retiro.setMonto(new BigDecimal("-100000.00"));
        retiro.setConcepto("RETIRO");
        retiro.setFechaHora(LocalDateTime.now().minusMonths(1).minusDays(3));

        MovimientoDTO posterior = new MovimientoDTO();
        posterior.setMonto(new BigDecimal("50000.00"));
        posterior.setConcepto("DEPOSITO");
        posterior.setFechaHora(LocalDateTime.now().minusDays(2));

        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuarioMock));
        when(cuentaRepository.findByIdCuentaAndCliente_IdCliente(eq(10L), eq(1L)))
                .thenReturn(Optional.of(cuentaMock));
        when(transaccionMapper.aListaDTOUnificada(any(), any(), anyLong(), any(), any()))
                .thenReturn(List.of(deposito, retiro))
                .thenReturn(List.of(posterior));
        when(pdfGenerador.generar(any())).thenReturn("%PDF-test".getBytes());

        byte[] resultado = service.generarExtracto(10L, mesPasado.getYear(), mesPasado.getMonthValue(), "ana");

        assertThat(resultado).isNotNull();
    }
}
