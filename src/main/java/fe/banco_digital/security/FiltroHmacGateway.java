package fe.banco_digital.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ReadListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

public class FiltroHmacGateway extends OncePerRequestFilter {

    private static final String CABECERA_FIRMA = "X-Gateway-Signature";
    private final byte[] secretoBytes;

    public FiltroHmacGateway(String secreto) {
        this.secretoBytes = secreto.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/v1/depositos/");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        SolicitudConCuerpoEnCache solicitudEnCache = new SolicitudConCuerpoEnCache(request);

        String firmaRecibida = request.getHeader(CABECERA_FIRMA);
        if (firmaRecibida == null || firmaRecibida.isBlank()) {
            rechazar(response, "Firma HMAC ausente. Se requiere el header X-Gateway-Signature.");
            return;
        }

        String firmaEsperada = calcularFirma(solicitudEnCache.getCuerpo());

        // DEBUG TEMPORAL — eliminar antes de commit
        System.out.println("[HMAC-DEBUG] body    : " + new String(solicitudEnCache.getCuerpo(), StandardCharsets.UTF_8));
        System.out.println("[HMAC-DEBUG] esperada: " + firmaEsperada);
        System.out.println("[HMAC-DEBUG] recibida: " + firmaRecibida);

        if (!MessageDigest.isEqual(firmaEsperada.getBytes(StandardCharsets.UTF_8),
                                   firmaRecibida.getBytes(StandardCharsets.UTF_8))) {
            rechazar(response, "Firma HMAC inválida. La notificación no proviene de una pasarela autorizada.");
            return;
        }

        filterChain.doFilter(solicitudEnCache, response);
    }

    private String calcularFirma(byte[] cuerpo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretoBytes, "HmacSHA256"));
            return "sha256=" + HexFormat.of().formatHex(mac.doFinal(cuerpo));
        } catch (Exception e) {
            throw new IllegalStateException("Error al calcular firma HMAC", e);
        }
    }

    private void rechazar(HttpServletResponse response, String mensaje) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.getWriter().write(
                "{\"timestamp\":\"" + LocalDateTime.now() + "\"," +
                "\"estado\":401," +
                "\"error\":\"Unauthorized\"," +
                "\"mensaje\":\"" + mensaje + "\"}"
        );
    }

    private static class SolicitudConCuerpoEnCache extends HttpServletRequestWrapper {

        private final byte[] cuerpo;

        SolicitudConCuerpoEnCache(HttpServletRequest request) throws IOException {
            super(request);
            cuerpo = request.getInputStream().readAllBytes();
        }

        byte[] getCuerpo() {
            return cuerpo;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(cuerpo);
            return new ServletInputStream() {
                public int read() { return bais.read(); }
                public boolean isFinished() { return bais.available() == 0; }
                public boolean isReady() { return true; }
                public void setReadListener(ReadListener l) {}
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }
    }
}
