package fe.banco_digital.controller;

import fe.banco_digital.dto.LoginRequestDTO;
import fe.banco_digital.dto.LoginResponseDTO;
import fe.banco_digital.dto.RegistroRequestDTO;
import fe.banco_digital.dto.UsuarioRegistradoDTO;
import fe.banco_digital.exception.TokenInvalidoException;
import fe.banco_digital.service.AutenticacionService;
import fe.banco_digital.service.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Login, registro y gestión de sesión")
public class AutenticacionController {

    private static final String COOKIE_ACCESS  = "accessToken";
    private static final String COOKIE_REFRESH = "refreshToken";

    @Value("${app.https:false}")
    private boolean httpsSeguro;

    private final AutenticacionService autenticacionService;
    private final RefreshTokenService refreshTokenService;

    public AutenticacionController(AutenticacionService autenticacionService,
                                   RefreshTokenService refreshTokenService) {
        this.autenticacionService = autenticacionService;
        this.refreshTokenService = refreshTokenService;
    }

    @Operation(summary = "Registrar usuario",
               description = "Crea las credenciales de acceso para un cliente existente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "409", description = "El username ya existe o el cliente ya tiene usuario"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PostMapping("/registro")
    public ResponseEntity<UsuarioRegistradoDTO> registrar(@RequestBody RegistroRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(autenticacionService.registrar(dto));
    }

    @Operation(summary = "Iniciar sesión",
               description = "Setea cookies HttpOnly con el access token (10 min) y el refresh token (7 días)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDTO dto,
                                                     HttpServletResponse response) {
        LoginResponseDTO tokens = autenticacionService.login(dto);
        setearCookies(response, tokens.getAccessToken(), tokens.getRefreshToken());
        return ResponseEntity.ok(Map.of("mensaje", "Sesión iniciada exitosamente"));
    }

    @Operation(summary = "Renovar token",
               description = "Lee el refresh token de la cookie y emite un nuevo par de tokens")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest request,
                                                       HttpServletResponse response) {
        String refreshToken = leerCookie(request, COOKIE_REFRESH);
        if (refreshToken == null) throw new TokenInvalidoException();

        LoginResponseDTO tokens = refreshTokenService.renovarToken(refreshToken);
        setearCookies(response, tokens.getAccessToken(), tokens.getRefreshToken());
        return ResponseEntity.ok(Map.of("mensaje", "Token renovado exitosamente"));
    }

    @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token y limpia las cookies")
    @ApiResponse(responseCode = "200", description = "Sesión cerrada")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request,
                                                      HttpServletResponse response) {
        String refreshToken = leerCookie(request, COOKIE_REFRESH);
        if (refreshToken != null) {
            refreshTokenService.revocarToken(refreshToken);
        }
        limpiarCookies(response);
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada exitosamente"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void setearCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        ResponseCookie cookieAccess = ResponseCookie.from(COOKIE_ACCESS, accessToken)
                .httpOnly(true)
                .path("/")
                .maxAge(600)
                .secure(httpsSeguro)
                .sameSite(httpsSeguro ? "Strict" : "Lax")
                .build();

        ResponseCookie cookieRefresh = ResponseCookie.from(COOKIE_REFRESH, refreshToken)
                .httpOnly(true)
                .path("/api/v1/auth/refresh")
                .maxAge(7L * 24 * 60 * 60)
                .secure(httpsSeguro)
                .sameSite(httpsSeguro ? "Strict" : "Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookieAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieRefresh.toString());
    }

    private void limpiarCookies(HttpServletResponse response) {
        ResponseCookie cookieAccess = ResponseCookie.from(COOKIE_ACCESS, "")
                .httpOnly(true).path("/").maxAge(0).sameSite("Lax").build();

        ResponseCookie cookieRefresh = ResponseCookie.from(COOKIE_REFRESH, "")
                .httpOnly(true).path("/api/v1/auth/refresh").maxAge(0).sameSite("Lax").build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookieAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieRefresh.toString());
    }

    private String leerCookie(HttpServletRequest request, String nombre) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> nombre.equals(c.getName()))
                .map(c -> c.getValue())
                .findFirst()
                .orElse(null);
    }
}
