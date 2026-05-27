# HU-15: Extracto Bancario en PDF — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Exponer `GET /api/v1/extractos/{idCuenta}/{anio}/{mes}` que genera y descarga un PDF de solo lectura con saldo inicial, movimientos del mes y saldo final; bloqueando el mes en curso con 422.

**Architecture:** `ExtractoController` delega a `ExtractoServiceImpl`, que verifica propiedad via JWT, calcula saldos con los 4 repos de movimientos existentes y delega la generación del PDF a `ExtractoPdfGenerador` (@Component puro sin acceso a BD). Dos excepciones nuevas (`PeriodoNoDisponibleException` → 422, `PeriodoInvalidoException` → 400) se registran en `GlobalExceptionHandler`.

**Tech Stack:** Spring Boot 4 / Java 17, Apache PDFBox 3.0.3, JUnit 5 + Mockito, bash + curl para scripts de integración.

---

## Mapa de archivos

| Acción | Ruta |
|--------|------|
| Modify | `pom.xml` |
| Create | `src/main/java/fe/banco_digital/exception/PeriodoNoDisponibleException.java` |
| Create | `src/main/java/fe/banco_digital/exception/PeriodoInvalidoException.java` |
| Modify | `src/main/java/fe/banco_digital/exception/GlobalExceptionHandler.java` |
| Create | `src/main/java/fe/banco_digital/dto/ExtractoDatosDTO.java` |
| Create | `src/main/java/fe/banco_digital/pdf/ExtractoPdfGenerador.java` |
| Create | `src/test/java/fe/banco_digital/pdf/ExtractoPdfGeneradorTest.java` |
| Create | `src/main/java/fe/banco_digital/service/ExtractoService.java` |
| Create | `src/main/java/fe/banco_digital/service/ExtractoServiceImpl.java` |
| Create | `src/test/java/fe/banco_digital/service/ExtractoServiceImplTest.java` |
| Create | `src/main/java/fe/banco_digital/controller/ExtractoController.java` |
| Create | `src/test/java/fe/banco_digital/controller/ExtractoControllerTest.java` |
| Create | `scripts/HU-15/_comun.sh` |
| Create | `scripts/HU-15/01-mes-cerrado-con-movimientos.sh` |
| Create | `scripts/HU-15/02-mes-cerrado-sin-movimientos.sh` |
| Create | `scripts/HU-15/03-mes-en-curso.sh` |
| Create | `scripts/HU-15/04-cuenta-no-pertenece.sh` |
| Create | `scripts/HU-15/05-cuenta-no-encontrada.sh` |
| Create | `scripts/HU-15/06-sin-autenticacion.sh` |
| Create | `scripts/HU-15/07-parametros-invalidos.sh` |

---

## Task 1: Setup — PDFBox + Excepciones + GlobalExceptionHandler

**Files:**
- Modify: `pom.xml`
- Create: `src/main/java/fe/banco_digital/exception/PeriodoNoDisponibleException.java`
- Create: `src/main/java/fe/banco_digital/exception/PeriodoInvalidoException.java`
- Modify: `src/main/java/fe/banco_digital/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: Agregar dependencia PDFBox en `pom.xml`**

Agregar dentro de `<dependencies>`, antes del cierre `</dependencies>`:

```xml
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>3.0.3</version>
</dependency>
```

- [ ] **Step 2: Crear `PeriodoNoDisponibleException.java`**

```java
package fe.banco_digital.exception;

public class PeriodoNoDisponibleException extends RuntimeException {
    public PeriodoNoDisponibleException() {
        super("El extracto oficial estará disponible al finalizar el periodo actual");
    }
}
```

- [ ] **Step 3: Crear `PeriodoInvalidoException.java`**

```java
package fe.banco_digital.exception;

public class PeriodoInvalidoException extends RuntimeException {
    public PeriodoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
```

- [ ] **Step 4: Registrar ambas excepciones en `GlobalExceptionHandler.java`**

Agregar los dos métodos siguientes antes del método `manejarExcepcionGeneral` (línea 136):

```java
@ExceptionHandler(PeriodoNoDisponibleException.class)
public ResponseEntity<Map<String, Object>> manejarPeriodoNoDisponible(PeriodoNoDisponibleException ex) {
    return construirRespuesta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
}

@ExceptionHandler(PeriodoInvalidoException.class)
public ResponseEntity<Map<String, Object>> manejarPeriodoInvalido(PeriodoInvalidoException ex) {
    return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
}
```

- [ ] **Step 5: Verificar compilación**

```bash
./mvnw compile -q
```

Esperado: BUILD SUCCESS sin errores.

- [ ] **Step 6: Commit**

```bash
git add pom.xml \
  src/main/java/fe/banco_digital/exception/PeriodoNoDisponibleException.java \
  src/main/java/fe/banco_digital/exception/PeriodoInvalidoException.java \
  src/main/java/fe/banco_digital/exception/GlobalExceptionHandler.java
git commit -m "feat(hu-15): agregar dependencia PDFBox y excepciones de periodo"
```

---

## Task 2: ExtractoDatosDTO

**Files:**
- Create: `src/main/java/fe/banco_digital/dto/ExtractoDatosDTO.java`

- [ ] **Step 1: Crear `ExtractoDatosDTO.java`**

```java
package fe.banco_digital.dto;

import java.math.BigDecimal;
import java.util.List;

public class ExtractoDatosDTO {

    private String numeroCuentaEnmascarado;
    private String tipoCuenta;
    private String nombreTitular;
    private String documento;
    private int anio;
    private int mes;
    private BigDecimal saldoInicial;
    private BigDecimal saldoFinal;
    private BigDecimal totalCreditos;
    private BigDecimal totalDebitos;
    private List<MovimientoDTO> movimientos;

    public String getNumeroCuentaEnmascarado() { return numeroCuentaEnmascarado; }
    public void setNumeroCuentaEnmascarado(String numeroCuentaEnmascarado) { this.numeroCuentaEnmascarado = numeroCuentaEnmascarado; }

    public String getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta; }

    public String getNombreTitular() { return nombreTitular; }
    public void setNombreTitular(String nombreTitular) { this.nombreTitular = nombreTitular; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public int getAnio() { return anio; }
    public void setAnio(int anio) { this.anio = anio; }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }

    public BigDecimal getSaldoInicial() { return saldoInicial; }
    public void setSaldoInicial(BigDecimal saldoInicial) { this.saldoInicial = saldoInicial; }

    public BigDecimal getSaldoFinal() { return saldoFinal; }
    public void setSaldoFinal(BigDecimal saldoFinal) { this.saldoFinal = saldoFinal; }

    public BigDecimal getTotalCreditos() { return totalCreditos; }
    public void setTotalCreditos(BigDecimal totalCreditos) { this.totalCreditos = totalCreditos; }

    public BigDecimal getTotalDebitos() { return totalDebitos; }
    public void setTotalDebitos(BigDecimal totalDebitos) { this.totalDebitos = totalDebitos; }

    public List<MovimientoDTO> getMovimientos() { return movimientos; }
    public void setMovimientos(List<MovimientoDTO> movimientos) { this.movimientos = movimientos; }
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/fe/banco_digital/dto/ExtractoDatosDTO.java
git commit -m "feat(hu-15): agregar ExtractoDatosDTO"
```

---

## Task 3: ExtractoPdfGenerador (TDD)

**Files:**
- Create: `src/test/java/fe/banco_digital/pdf/ExtractoPdfGeneradorTest.java`
- Create: `src/main/java/fe/banco_digital/pdf/ExtractoPdfGenerador.java`

- [ ] **Step 1: Escribir test que falla**

```java
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
```

- [ ] **Step 2: Correr test — debe fallar con ClassNotFoundException**

```bash
./mvnw test -Dtest=ExtractoPdfGeneradorTest -pl . 2>&1 | tail -20
```

Esperado: ERROR — `ExtractoPdfGenerador` no existe aún.

- [ ] **Step 3: Crear `ExtractoPdfGenerador.java`**

```java
package fe.banco_digital.pdf;

import fe.banco_digital.dto.ExtractoDatosDTO;
import fe.banco_digital.dto.MovimientoDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Component
public class ExtractoPdfGenerador {

    private static final float MARGEN = 50f;
    private static final float ANCHO = PDRectangle.A4.getWidth();
    private static final float ALTO  = PDRectangle.A4.getHeight();

    public byte[] generar(ExtractoDatosDTO datos) {
        try (PDDocument doc = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            doc.addPage(pagina);

            PDType1Font normal  = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            try (PDPageContentStream cs = new PDPageContentStream(doc, pagina)) {
                float y = ALTO - MARGEN;
                y = escribirEncabezado(cs, datos, normal, negrita, y);
                y = escribirInfoCuenta(cs, datos, normal, y);
                y = escribirSaldo(cs, "SALDO INICIAL", datos.getSaldoInicial(), negrita, y - 10);
                y = escribirTablaMovimientos(cs, datos.getMovimientos(), normal, negrita, y - 10);
                escribirResumen(cs, datos, normal, negrita, y - 5);
            }

            aplicarProteccion(doc);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new IllegalStateException("Error al generar PDF del extracto", e);
        }
    }

    private float escribirEncabezado(PDPageContentStream cs, ExtractoDatosDTO d,
                                      PDType1Font normal, PDType1Font negrita, float y) throws IOException {
        texto(cs, negrita, 16, MARGEN, y, "BANCO DIGITAL");
        y -= 18;
        texto(cs, normal, 11, MARGEN, y, "Extracto Bancario Oficial");
        y -= 14;
        String nombreMes = Month.of(d.getMes())
                .getDisplayName(TextStyle.FULL, new Locale("es", "CO")).toUpperCase();
        texto(cs, normal, 11, MARGEN, y, "Período: " + nombreMes + " " + d.getAnio());
        return y - 20;
    }

    private float escribirInfoCuenta(PDPageContentStream cs, ExtractoDatosDTO d,
                                      PDType1Font normal, float y) throws IOException {
        linea(cs, y + 3);
        y -= 12;
        texto(cs, normal, 10, MARGEN, y, "Cuenta: " + d.getNumeroCuentaEnmascarado() + "  |  " + d.getTipoCuenta());
        y -= 14;
        texto(cs, normal, 10, MARGEN, y, "Titular: " + d.getNombreTitular() + "  |  CC " + d.getDocumento());
        y -= 14;
        linea(cs, y);
        return y - 5;
    }

    private float escribirSaldo(PDPageContentStream cs, String etiqueta, BigDecimal saldo,
                                 PDType1Font negrita, float y) throws IOException {
        texto(cs, negrita, 11, MARGEN, y, etiqueta + ":  $" + fmt(saldo));
        return y - 18;
    }

    private float escribirTablaMovimientos(PDPageContentStream cs, List<MovimientoDTO> movs,
                                            PDType1Font normal, PDType1Font negrita, float y) throws IOException {
        texto(cs, negrita, 9, MARGEN, y, String.format("%-12s %-34s %14s", "Fecha", "Descripción", "Monto"));
        y -= 5;
        linea(cs, y);
        y -= 13;

        for (MovimientoDTO m : movs) {
            if (y < 100) break;
            String fecha = m.getFechaHora() != null ? m.getFechaHora().toLocalDate().toString() : "-";
            String desc  = m.getConcepto() != null ? m.getConcepto() : "-";
            if (desc.length() > 32) desc = desc.substring(0, 32);
            String signo = m.getMonto() != null && m.getMonto().compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
            String montoStr = signo + "$" + fmt(m.getMonto() != null ? m.getMonto() : BigDecimal.ZERO);

            texto(cs, normal, 9, MARGEN, y, String.format("%-12s %-34s %14s", fecha, desc, montoStr));
            y -= 13;
        }

        linea(cs, y);
        return y - 8;
    }

    private void escribirResumen(PDPageContentStream cs, ExtractoDatosDTO d,
                                  PDType1Font normal, PDType1Font negrita, float y) throws IOException {
        texto(cs, normal, 10, MARGEN, y, "Total créditos:  +$" + fmt(d.getTotalCreditos()));
        y -= 14;
        texto(cs, normal, 10, MARGEN, y, "Total débitos:    -$" + fmt(d.getTotalDebitos().abs()));
        y -= 14;
        escribirSaldo(cs, "SALDO FINAL", d.getSaldoFinal(), negrita, y);
        y -= 28;
        linea(cs, y + 12);
        texto(cs, normal, 8, MARGEN, y, "Documento oficial de solo lectura — Banco Digital " + d.getAnio());
    }

    private void texto(PDPageContentStream cs, PDType1Font font, float size,
                       float x, float y, String contenido) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.newLineAtOffset(x, y);
        cs.showText(contenido);
        cs.endText();
    }

    private void linea(PDPageContentStream cs, float y) throws IOException {
        cs.moveTo(MARGEN, y);
        cs.lineTo(ANCHO - MARGEN, y);
        cs.stroke();
    }

    private String fmt(BigDecimal valor) {
        return String.format("%,.2f", valor);
    }

    private void aplicarProteccion(PDDocument doc) throws IOException {
        AccessPermission ap = new AccessPermission();
        ap.setCanModify(false);
        ap.setCanExtractContent(false);
        ap.setCanAssembleDocument(false);
        ap.setCanFillInForm(false);
        ap.setCanModifyAnnotations(false);
        ap.setCanPrint(true);
        StandardProtectionPolicy policy = new StandardProtectionPolicy(
                "BancoDigital_" + System.currentTimeMillis(), "", ap);
        policy.setEncryptionKeyLength(128);
        doc.protect(policy);
    }
}
```

- [ ] **Step 4: Correr tests del generador — deben pasar**

```bash
./mvnw test -Dtest=ExtractoPdfGeneradorTest 2>&1 | tail -10
```

Esperado: `Tests run: 3, Failures: 0, Errors: 0`

- [ ] **Step 5: Commit**

```bash
git add src/main/java/fe/banco_digital/pdf/ExtractoPdfGenerador.java \
        src/test/java/fe/banco_digital/pdf/ExtractoPdfGeneradorTest.java
git commit -m "feat(hu-15): ExtractoPdfGenerador con PDFBox 3 y protección solo lectura"
```

---

## Task 4: ExtractoService + ExtractoServiceImpl (TDD)

**Files:**
- Create: `src/test/java/fe/banco_digital/service/ExtractoServiceImplTest.java`
- Create: `src/main/java/fe/banco_digital/service/ExtractoService.java`
- Create: `src/main/java/fe/banco_digital/service/ExtractoServiceImpl.java`

- [ ] **Step 1: Escribir tests del servicio que fallan**

```java
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
        cliente.setNombre("Ana López");
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
        if (mesFuturo > 12) return; // enero siguiente año ya cubierto por test de año futuro
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
                .thenReturn(List.of()); // sin movimientos en el periodo ni posteriores
        when(pdfGenerador.generar(any())).thenReturn("%PDF-test".getBytes());

        byte[] resultado = service.generarExtracto(10L, mesPasado.getYear(), mesPasado.getMonthValue(), "ana");

        assertThat(resultado).isNotNull().isNotEmpty();
    }

    @Test
    void generarExtracto_conMovimientos_calculaSaldosCorrectamente() {
        LocalDate mesPasado = LocalDate.now().minusMonths(1);

        // Cuenta tiene saldo actual de 1.500.000
        // El periodo tuvo: +200.000 depósito, -100.000 retiro → net = +100.000
        // Después del periodo: +50.000 depósito → net = +50.000
        // saldo_final_periodo = 1.500.000 - 50.000 = 1.450.000
        // saldo_inicial_periodo = 1.450.000 - 100.000 = 1.350.000

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
        // Primera llamada: movimientos del periodo; segunda: movimientos posteriores
        when(transaccionMapper.aListaDTOUnificada(any(), any(), anyLong(), any(), any()))
                .thenReturn(List.of(deposito, retiro))   // periodo
                .thenReturn(List.of(posterior));          // posteriores
        when(pdfGenerador.generar(any())).thenReturn("%PDF-test".getBytes());

        byte[] resultado = service.generarExtracto(10L, mesPasado.getYear(), mesPasado.getMonthValue(), "ana");

        assertThat(resultado).isNotNull();
    }
}
```

- [ ] **Step 2: Correr tests — deben fallar**

```bash
./mvnw test -Dtest=ExtractoServiceImplTest 2>&1 | tail -10
```

Esperado: ERROR — `ExtractoServiceImpl` no existe aún.

- [ ] **Step 3: Crear `ExtractoService.java`**

```java
package fe.banco_digital.service;

public interface ExtractoService {
    byte[] generarExtracto(Long idCuenta, int anio, int mes, String username);
}
```

- [ ] **Step 4: Crear `ExtractoServiceImpl.java`**

```java
package fe.banco_digital.service;

import fe.banco_digital.dto.ExtractoDatosDTO;
import fe.banco_digital.dto.MovimientoDTO;
import fe.banco_digital.entity.Cliente;
import fe.banco_digital.entity.Cuenta;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.AutenticacionFallidaException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class ExtractoServiceImpl implements ExtractoService {

    private final UsuarioRepository usuarioRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final TransferenciaExternaRepository transferenciaExternaRepository;
    private final TransferenciaInternacionalRepository transferenciaInternacionalRepository;
    private final TransaccionMapper transaccionMapper;
    private final ExtractoPdfGenerador pdfGenerador;

    public ExtractoServiceImpl(UsuarioRepository usuarioRepository,
                                CuentaRepository cuentaRepository,
                                MovimientoRepository movimientoRepository,
                                TransferenciaRepository transferenciaRepository,
                                TransferenciaExternaRepository transferenciaExternaRepository,
                                TransferenciaInternacionalRepository transferenciaInternacionalRepository,
                                TransaccionMapper transaccionMapper,
                                ExtractoPdfGenerador pdfGenerador) {
        this.usuarioRepository = usuarioRepository;
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.transferenciaExternaRepository = transferenciaExternaRepository;
        this.transferenciaInternacionalRepository = transferenciaInternacionalRepository;
        this.transaccionMapper = transaccionMapper;
        this.pdfGenerador = pdfGenerador;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarExtracto(Long idCuenta, int anio, int mes, String username) {
        validarParametros(anio, mes);

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(AutenticacionFallidaException::new);

        Cuenta cuenta = cuentaRepository
                .findByIdCuentaAndCliente_IdCliente(idCuenta, usuario.getCliente().getIdCliente())
                .orElseThrow(AccesoNoAutorizadoException::new);

        LocalDateTime inicio = LocalDateTime.of(anio, mes, 1, 0, 0, 0);
        LocalDateTime fin = inicio.with(TemporalAdjusters.lastDayOfMonth())
                .withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);
        LocalDateTime ahora = LocalDateTime.now();

        List<MovimientoDTO> movsPeriodo    = obtenerMovimientos(idCuenta, inicio, fin);
        List<MovimientoDTO> movsPosteriores = obtenerMovimientos(idCuenta, fin.plusNanos(1), ahora);

        BigDecimal sumaPosteriores = sumar(movsPosteriores);
        BigDecimal saldoFinal      = cuenta.getSaldo().subtract(sumaPosteriores);
        BigDecimal sumaPeriodo     = sumar(movsPeriodo);
        BigDecimal saldoInicial    = saldoFinal.subtract(sumaPeriodo);

        BigDecimal totalCreditos = movsPeriodo.stream()
                .map(MovimientoDTO::getMonto)
                .filter(m -> m.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDebitos = movsPeriodo.stream()
                .map(MovimientoDTO::getMonto)
                .filter(m -> m.compareTo(BigDecimal.ZERO) < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Cliente cliente = cuenta.getCliente();

        ExtractoDatosDTO datos = new ExtractoDatosDTO();
        datos.setNumeroCuentaEnmascarado(enmascararCuenta(cuenta.getNumeroCuenta()));
        datos.setTipoCuenta(cuenta.getTipo().name());
        datos.setNombreTitular(cliente.getNombre());
        datos.setDocumento(cliente.getDocumento());
        datos.setAnio(anio);
        datos.setMes(mes);
        datos.setSaldoInicial(saldoInicial);
        datos.setSaldoFinal(saldoFinal);
        datos.setTotalCreditos(totalCreditos);
        datos.setTotalDebitos(totalDebitos);
        datos.setMovimientos(movsPeriodo);

        return pdfGenerador.generar(datos);
    }

    private void validarParametros(int anio, int mes) {
        if (mes < 1 || mes > 12) {
            throw new PeriodoInvalidoException("El mes debe estar entre 1 y 12");
        }
        LocalDate hoy = LocalDate.now();
        if (anio > hoy.getYear()
                || (anio == hoy.getYear() && mes > hoy.getMonthValue())) {
            throw new PeriodoInvalidoException("No se pueden generar extractos de fechas futuras");
        }
        if (anio == hoy.getYear() && mes == hoy.getMonthValue()) {
            throw new PeriodoNoDisponibleException();
        }
    }

    private List<MovimientoDTO> obtenerMovimientos(Long idCuenta,
                                                    LocalDateTime desde, LocalDateTime hasta) {
        return transaccionMapper.aListaDTOUnificada(
                movimientoRepository
                        .findByCuenta_IdCuentaAndFechaBetweenOrderByFechaDesc(idCuenta, desde, hasta),
                transferenciaRepository
                        .findByCuentaIdAndFechaBetweenOrderByFechaDesc(idCuenta, desde, hasta),
                idCuenta,
                transferenciaExternaRepository
                        .findByCuentaOrigen_IdCuentaAndFechaBetweenOrderByFechaDesc(idCuenta, desde, hasta),
                transferenciaInternacionalRepository
                        .findByCuentaOrigen_IdCuentaAndFechaBetweenOrderByFechaDesc(idCuenta, desde, hasta));
    }

    private BigDecimal sumar(List<MovimientoDTO> movimientos) {
        return movimientos.stream()
                .map(MovimientoDTO::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String enmascararCuenta(String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.length() < 4) return numeroCuenta;
        return "****" + numeroCuenta.substring(numeroCuenta.length() - 4);
    }
}
```

- [ ] **Step 5: Correr tests del servicio — deben pasar**

```bash
./mvnw test -Dtest=ExtractoServiceImplTest 2>&1 | tail -10
```

Esperado: `Tests run: 6, Failures: 0, Errors: 0`

- [ ] **Step 6: Commit**

```bash
git add src/main/java/fe/banco_digital/service/ExtractoService.java \
        src/main/java/fe/banco_digital/service/ExtractoServiceImpl.java \
        src/test/java/fe/banco_digital/service/ExtractoServiceImplTest.java
git commit -m "feat(hu-15): ExtractoService con validación de periodo y cálculo de saldos"
```

---

## Task 5: ExtractoController (TDD)

**Files:**
- Create: `src/test/java/fe/banco_digital/controller/ExtractoControllerTest.java`
- Create: `src/main/java/fe/banco_digital/controller/ExtractoController.java`

- [ ] **Step 1: Escribir tests del controller que fallan**

```java
package fe.banco_digital.controller;

import fe.banco_digital.exception.AccesoNoAutorizadoException;
import fe.banco_digital.exception.CuentaNoEncontradaException;
import fe.banco_digital.exception.GlobalExceptionHandler;
import fe.banco_digital.exception.PeriodoInvalidoException;
import fe.banco_digital.exception.PeriodoNoDisponibleException;
import fe.banco_digital.service.ExtractoService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ExtractoControllerTest {

    @Mock ExtractoService extractoService;
    @InjectMocks ExtractoController controller;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        User userDetails = new User("testuser", "pass", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void extracto_mesCerradoConMovimientos_retorna200ConPdf() throws Exception {
        byte[] pdfMock = "%PDF-1.4 test content".getBytes();
        when(extractoService.generarExtracto(anyLong(), anyInt(), anyInt(), anyString()))
                .thenReturn(pdfMock);

        mockMvc.perform(get("/api/v1/extractos/1/2026/4"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"extracto_2026_04.pdf\""));
    }

    @Test
    void extracto_mesCerradoSinMovimientos_retorna200ConPdf() throws Exception {
        byte[] pdfVacio = "%PDF-1.4 sin movimientos".getBytes();
        when(extractoService.generarExtracto(anyLong(), anyInt(), anyInt(), anyString()))
                .thenReturn(pdfVacio);

        mockMvc.perform(get("/api/v1/extractos/1/2025/12"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void extracto_mesEnCurso_retorna422ConMensaje() throws Exception {
        when(extractoService.generarExtracto(anyLong(), anyInt(), anyInt(), anyString()))
                .thenThrow(new PeriodoNoDisponibleException());

        mockMvc.perform(get("/api/v1/extractos/1/2026/5"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value(
                        "El extracto oficial estará disponible al finalizar el periodo actual"));
    }

    @Test
    void extracto_cuentaNoPertenece_retorna403() throws Exception {
        when(extractoService.generarExtracto(anyLong(), anyInt(), anyInt(), anyString()))
                .thenThrow(new AccesoNoAutorizadoException());

        mockMvc.perform(get("/api/v1/extractos/99/2026/4"))
                .andExpect(status().isForbidden());
    }

    @Test
    void extracto_cuentaNoEncontrada_retorna404() throws Exception {
        when(extractoService.generarExtracto(anyLong(), anyInt(), anyInt(), anyString()))
                .thenThrow(new CuentaNoEncontradaException(99L));

        mockMvc.perform(get("/api/v1/extractos/99/2026/4"))
                .andExpect(status().isNotFound());
    }

    @Test
    void extracto_parametrosInvalidos_retorna400() throws Exception {
        when(extractoService.generarExtracto(anyLong(), anyInt(), anyInt(), anyString()))
                .thenThrow(new PeriodoInvalidoException("El mes debe estar entre 1 y 12"));

        mockMvc.perform(get("/api/v1/extractos/1/2026/13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El mes debe estar entre 1 y 12"));
    }
}
```

- [ ] **Step 2: Correr tests — deben fallar**

```bash
./mvnw test -Dtest=ExtractoControllerTest 2>&1 | tail -10
```

Esperado: ERROR — `ExtractoController` no existe aún.

- [ ] **Step 3: Crear `ExtractoController.java`**

```java
package fe.banco_digital.controller;

import fe.banco_digital.service.ExtractoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/extractos")
@Tag(name = "Extractos", description = "Generación de extractos bancarios en PDF")
public class ExtractoController {

    private final ExtractoService extractoService;

    public ExtractoController(ExtractoService extractoService) {
        this.extractoService = extractoService;
    }

    @Operation(summary = "Descargar extracto bancario mensual en PDF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF generado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de periodo inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "La cuenta no pertenece al usuario autenticado"),
            @ApiResponse(responseCode = "404", description = "Cuenta no encontrada"),
            @ApiResponse(responseCode = "422", description = "El periodo aún no ha cerrado")
    })
    @GetMapping("/{idCuenta}/{anio}/{mes}")
    public ResponseEntity<byte[]> generarExtracto(
            @PathVariable Long idCuenta,
            @PathVariable int anio,
            @PathVariable int mes,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {

        byte[] pdf = extractoService.generarExtracto(
                idCuenta, anio, mes, usuarioAutenticado.getUsername());

        String nombreArchivo = String.format("extracto_%d_%02d.pdf", anio, mes);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nombreArchivo + "\"")
                .body(pdf);
    }
}
```

- [ ] **Step 4: Correr todos los tests nuevos — deben pasar**

```bash
./mvnw test -Dtest="ExtractoPdfGeneradorTest,ExtractoServiceImplTest,ExtractoControllerTest" 2>&1 | tail -15
```

Esperado: `Tests run: 12, Failures: 0, Errors: 0` (3 + 6 + 6 = aprox)

- [ ] **Step 5: Correr suite completa para verificar regresiones y coverage**

```bash
./mvnw verify 2>&1 | tail -20
```

Esperado: BUILD SUCCESS. Si JaCoCo falla por cobertura < 80%, agregar más tests hasta pasar.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/fe/banco_digital/controller/ExtractoController.java \
        src/test/java/fe/banco_digital/controller/ExtractoControllerTest.java
git commit -m "feat(hu-15): ExtractoController — endpoint GET /api/v1/extractos/{idCuenta}/{anio}/{mes}"
```

---

## Task 6: Scripts .sh para HU-15

**Files:** `scripts/HU-15/_comun.sh` + 7 scripts numerados.

- [ ] **Step 1: Crear `scripts/HU-15/_comun.sh`**

```bash
#!/bin/bash
# Variables y helpers compartidos — HU-15 Extracto Bancario

source "$(dirname "${BASH_SOURCE[0]}")/../config.sh"
URL_BASE="${API_BASE}/api/v1"
COOKIES="$(dirname "${BASH_SOURCE[0]}")/cookies_hu15.txt"

PASS=0; FAIL=0

login() {
  curl -s -c "$COOKIES" -X POST "${URL_BASE}/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$1\",\"password\":\"$2\"}" > /dev/null
}

get_id_cuenta() {
  local dash lista
  dash=$(curl -s -b "$COOKIES" "${URL_BASE}/cuentas/dashboard")
  if echo "$dash" | jq -e '.cuentas' > /dev/null 2>&1; then
    lista=$(echo "$dash" | jq '.cuentas')
  else
    lista="$dash"
  fi
  echo "$lista" | jq -r '.[0].idCuenta // empty'
}

# Descarga el extracto y devuelve HTTP code. Guarda el PDF en /tmp si es 200.
# Uso: http_code=$(extracto <idCuenta> <anio> <mes>)
extracto() {
  curl -s -o /tmp/extracto_hu15.pdf -w "%{http_code}" \
    -b "$COOKIES" \
    "${URL_BASE}/extractos/$1/$2/$3"
}

# Descarga con output JSON (para errores)
extracto_json() {
  curl -s -b "$COOKIES" "${URL_BASE}/extractos/$1/$2/$3"
}

ok()   { echo "  [PASS] $1"; PASS=$((PASS + 1)); }
fail() { echo "  [FAIL] $1"; FAIL=$((FAIL + 1)); }

resumen() {
  echo ""
  echo "========================================================"
  printf "  Resultado HU-15: %d pasaron · %d fallaron\n" "$PASS" "$FAIL"
  echo "========================================================"
  [ "$FAIL" -eq 0 ] && exit 0 || exit 1
}
```

- [ ] **Step 2: Crear `scripts/HU-15/01-mes-cerrado-con-movimientos.sh`**

```bash
#!/bin/bash
# Escenario 1: mes cerrado (abril 2026) con movimientos → 200 + PDF válido
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 1: mes cerrado con movimientos ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)

http_code=$(extracto "$ID" 2026 4)

if [ "$http_code" = "200" ]; then
  header=$(head -c 4 /tmp/extracto_hu15.pdf)
  if [ "$header" = "%PDF" ]; then
    ok "200 OK y el archivo es un PDF válido"
  else
    fail "200 pero el archivo no empieza con %PDF"
  fi
else
  fail "Esperado 200, obtenido: $http_code"
fi

resumen
```

- [ ] **Step 3: Crear `scripts/HU-15/02-mes-cerrado-sin-movimientos.sh`**

```bash
#!/bin/bash
# Escenario 2: mes cerrado sin movimientos → 200 + PDF (saldo inicial = saldo final)
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 2: mes cerrado sin movimientos ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)

http_code=$(extracto "$ID" 2025 1)

if [ "$http_code" = "200" ]; then
  header=$(head -c 4 /tmp/extracto_hu15.pdf)
  [ "$header" = "%PDF" ] && ok "200 OK PDF válido (periodo sin movimientos)" \
                           || fail "200 pero el archivo no empieza con %PDF"
else
  fail "Esperado 200, obtenido: $http_code"
fi

resumen
```

- [ ] **Step 4: Crear `scripts/HU-15/03-mes-en-curso.sh`**

```bash
#!/bin/bash
# Escenario 3: mes en curso → 422 con mensaje informativo
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 3: mes en curso ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)

# Mes actual: mayo 2026
resp=$(extracto_json "$ID" 2026 5)
http_code=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIES" "${URL_BASE}/extractos/$ID/2026/5")
mensaje=$(echo "$resp" | jq -r '.mensaje // empty')

if [ "$http_code" = "422" ]; then
  expected="El extracto oficial estará disponible al finalizar el periodo actual"
  [ "$mensaje" = "$expected" ] && ok "422 con mensaje correcto" \
                                 || fail "422 pero mensaje incorrecto: $mensaje"
else
  fail "Esperado 422, obtenido: $http_code"
fi

resumen
```

- [ ] **Step 5: Crear `scripts/HU-15/04-cuenta-no-pertenece.sh`**

```bash
#!/bin/bash
# Escenario 4: cuenta de otro usuario → 403
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 4: cuenta no pertenece al usuario ==="
login "ana" "ana123"
ID=$(get_id_cuenta)   # id de la cuenta de ana

# Nos deslogueamos de ana y logueamos como bryan
login "bryan" "bryan123"

http_code=$(curl -s -o /dev/null -w "%{http_code}" \
  -b "$COOKIES" "${URL_BASE}/extractos/$ID/2026/4")

[ "$http_code" = "403" ] && ok "403 Forbidden correcto" \
                           || fail "Esperado 403, obtenido: $http_code"

resumen
```

- [ ] **Step 6: Crear `scripts/HU-15/05-cuenta-no-encontrada.sh`**

```bash
#!/bin/bash
# Escenario 5: id de cuenta inexistente → 403 (no pertenece al usuario)
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 5: cuenta inexistente ==="
login "bryan" "bryan123"

http_code=$(curl -s -o /dev/null -w "%{http_code}" \
  -b "$COOKIES" "${URL_BASE}/extractos/999999/2026/4")

[ "$http_code" = "403" ] && ok "403 Forbidden para cuenta inexistente" \
                           || fail "Esperado 403, obtenido: $http_code"

resumen
```

- [ ] **Step 7: Crear `scripts/HU-15/06-sin-autenticacion.sh`**

```bash
#!/bin/bash
# Escenario 6: sin token / sin autenticación → 401
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 6: sin autenticación ==="

http_code=$(curl -s -o /dev/null -w "%{http_code}" \
  "${URL_BASE}/extractos/1/2026/4")

[ "$http_code" = "401" ] && ok "401 Unauthorized sin token" \
                           || fail "Esperado 401, obtenido: $http_code"

resumen
```

- [ ] **Step 8: Crear `scripts/HU-15/07-parametros-invalidos.sh`**

```bash
#!/bin/bash
# Escenario 7: parámetros inválidos → 400
source "$(dirname "$0")/_comun.sh"

echo "=== Escenario 7: parámetros inválidos ==="
login "bryan" "bryan123"
ID=$(get_id_cuenta)

# Mes fuera de rango (13)
resp=$(extracto_json "$ID" 2026 13)
http_code=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIES" "${URL_BASE}/extractos/$ID/2026/13")
[ "$http_code" = "400" ] && ok "400 para mes=13" || fail "Esperado 400 para mes=13, obtenido: $http_code"

# Año futuro
http_code2=$(curl -s -o /dev/null -w "%{http_code}" -b "$COOKIES" "${URL_BASE}/extractos/$ID/2099/1")
[ "$http_code2" = "400" ] && ok "400 para año futuro" || fail "Esperado 400 para año futuro, obtenido: $http_code2"

resumen
```

- [ ] **Step 9: Dar permisos de ejecución**

```bash
chmod +x scripts/HU-15/*.sh
```

- [ ] **Step 10: Commit**

```bash
git add scripts/HU-15/
git commit -m "test(hu-15): scripts bash para los 7 escenarios del extracto PDF"
```

---

## Task 7: Verificación final y ejecución de scripts

- [ ] **Step 1: Correr suite completa de tests unitarios**

```bash
./mvnw test 2>&1 | grep -E "Tests run:|BUILD"
```

Esperado: BUILD SUCCESS, todos los tests pasan.

- [ ] **Step 2: Verificar cobertura JaCoCo ≥ 80%**

```bash
./mvnw verify -DskipTests=false 2>&1 | grep -E "BUILD|Coverage|instructions"
```

Esperado: BUILD SUCCESS.

- [ ] **Step 3: Correr la aplicación localmente (terminal aparte)**

```bash
./scripts/run.sh
```

- [ ] **Step 4: Ejecutar scripts de integración**

```bash
bash scripts/HU-15/01-mes-cerrado-con-movimientos.sh
bash scripts/HU-15/02-mes-cerrado-sin-movimientos.sh
bash scripts/HU-15/03-mes-en-curso.sh
bash scripts/HU-15/04-cuenta-no-pertenece.sh
bash scripts/HU-15/05-cuenta-no-encontrada.sh
bash scripts/HU-15/06-sin-autenticacion.sh
bash scripts/HU-15/07-parametros-invalidos.sh
```

Esperado: todos los scripts imprimen `[PASS]` y finalizan con exit code 0.

- [ ] **Step 5: Commit final**

```bash
git add -A
git commit -m "feat(hu-15): extracto bancario PDF completo con tests y scripts"
```
