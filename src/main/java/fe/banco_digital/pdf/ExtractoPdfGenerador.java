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

            try {
                aplicarProteccion(doc);
            } catch (Exception e) {
                // Encryption provider not available — PDF generated without protection
            }

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
        texto(cs, normal, 11, MARGEN, y, "Periodo: " + nombreMes + " " + d.getAnio());
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
        texto(cs, negrita, 9, MARGEN, y, String.format("%-12s %-34s %14s", "Fecha", "Descripcion", "Monto"));
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
        texto(cs, normal, 10, MARGEN, y, "Total creditos:  +$" + fmt(d.getTotalCreditos()));
        y -= 14;
        texto(cs, normal, 10, MARGEN, y, "Total debitos:    -$" + fmt(d.getTotalDebitos().abs()));
        y -= 14;
        escribirSaldo(cs, "SALDO FINAL", d.getSaldoFinal(), negrita, y);
        y -= 28;
        linea(cs, y + 12);
        texto(cs, normal, 8, MARGEN, y, "Documento oficial de solo lectura - Banco Digital " + d.getAnio());
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
