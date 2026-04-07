package fe.banco_digital.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

public class FiltroJwt extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UsuarioDetallesService usuarioDetallesService;

    public FiltroJwt(JwtUtil jwtUtil, UsuarioDetallesService usuarioDetallesService) {
        this.jwtUtil = jwtUtil;
        this.usuarioDetallesService = usuarioDetallesService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String token = leerTokenDeCookie(request);

        if (token == null) {
            token = leerTokenDeHeader(request);
        }

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtUtil.extraerUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails detallesUsuario = usuarioDetallesService.loadUserByUsername(username);

                if (jwtUtil.esValido(token, username)) {
                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(
                                    detallesUsuario,
                                    null,
                                    detallesUsuario.getAuthorities()
                            );
                    autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            }
        } catch (ExpiredJwtException | MalformedJwtException | SignatureException ignored) {
            // Token inválido: se deja pasar sin autenticar.
        }

        filterChain.doFilter(request, response);
    }

    private String leerTokenDeCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "accessToken".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private String leerTokenDeHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
