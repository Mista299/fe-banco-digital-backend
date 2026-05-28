package fe.banco_digital.pdf;

import fe.banco_digital.dto.ExtractoDatosDTO;
import fe.banco_digital.dto.MovimientoDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExtractoPdfGeneradorTest {

    private final ExtractoPdfGenerador generador = new ExtractoPdfGenerador();

    private ExtractoDatosDTO datosMuestra(List<MovimientoDTO> movimientos) {
        ExtractoDatosDTO d = new ExtractoDatosDTO();
        d.setNumeroCuentaEnmascarado("****1234");
        d.setTipoCuenta("AHORROS");
        d.setNombreTitular("Juan Pérez");
        d.setDocumento("987654321");
        d.setAnio(2026);
        d.setMes(4);
        d.setSaldoInicial(new BigDecimal("1000000.00"));
        d.setSaldoFinal(new BigDecimal("1100000.00"));
        d.setTotalCreditos(new BigDecimal("200000.00"));
        d.setTotalDebitos(new BigDecimal("-100000.00"));
        d.setMovimientos(movimientos);
        return d;
    }

    @Test
    void generar_sinMovimientos_retornaPdfValido() {
        byte[] resultado = generador.generar(datosMuestra(List.of()));

        assertThat(resultado).isNotNull().isNotEmpty();
        assertThat(new String(resultado, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void generar_conMovimientos_retornaPdfValido() {
        MovimientoDTO mov1 = new MovimientoDTO();
        mov1.setFechaHora(LocalDateTime.of(2026, 4, 5, 10, 0));
        mov1.setConcepto("DEPOSITO");
        mov1.setMonto(new BigDecimal("200000.00"));

        MovimientoDTO mov2 = new MovimientoDTO();
        mov2.setFechaHora(LocalDateTime.of(2026, 4, 10, 15, 30));
        mov2.setConcepto("RETIRO");
        mov2.setMonto(new BigDecimal("-100000.00"));

        byte[] resultado = generador.generar(datosMuestra(List.of(mov1, mov2)));

        assertThat(resultado).isNotNull().isNotEmpty();
        assertThat(new String(resultado, 0, 4)).isEqualTo("%PDF");
    }

    @Test
    void generar_conDescripcionLarga_truncaYNoCrash() {
        MovimientoDTO mov = new MovimientoDTO();
        mov.setFechaHora(LocalDateTime.of(2026, 4, 1, 8, 0));
        mov.setConcepto("TRANSFERENCIA_INTERNACIONAL_DESCRIPCION_MUY_LARGA_QUE_SUPERA_EL_LIMITE");
        mov.setMonto(new BigDecimal("-50000.00"));

        byte[] resultado = generador.generar(datosMuestra(List.of(mov)));

        assertThat(resultado).isNotNull().isNotEmpty();
    }
}
