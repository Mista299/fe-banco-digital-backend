package fe.banco_digital.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FiltroJwtTest {

    @Mock
    JwtUtil jwtUtil;

    @Mock
    UsuarioDetallesService usuarioDetallesService;

    @Mock
    FilterChain filterChain;

    @InjectMocks
    FiltroJwt filtro;

    @BeforeEach
    void before() {
        // ensure clean security context
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @AfterEach
    void after() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_setsAuthentication_whenTokenValidInHeader() throws Exception {
        String token = "tok";
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(jwtUtil.extraerUsername(token)).thenReturn("u1");
        when(jwtUtil.esValido(token, "u1")).thenReturn(true);

        UserDetails ud = User.withUsername("u1").password("p").authorities(Collections.emptyList()).build();
        when(usuarioDetallesService.loadUserByUsername("u1")).thenReturn(ud);

        filtro.doFilterInternal(req, res, filterChain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("u1", auth.getName());
        verify(filterChain, atLeastOnce()).doFilter(req, res);
    }

    @Test
    void doFilter_passesWhenNoToken() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        filtro.doFilterInternal(req, res, filterChain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
        verify(filterChain).doFilter(req, res);
    }

    @Test
    void doFilter_readsTokenFromCookie_andSetsAuthentication() throws Exception {
        String token = "cookietok";
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new jakarta.servlet.http.Cookie("accessToken", token));
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(jwtUtil.extraerUsername(token)).thenReturn("cookieUser");
        when(jwtUtil.esValido(token, "cookieUser")).thenReturn(true);
        UserDetails ud = User.withUsername("cookieUser").password("p").authorities(Collections.emptyList()).build();
        when(usuarioDetallesService.loadUserByUsername("cookieUser")).thenReturn(ud);

        filtro.doFilterInternal(req, res, filterChain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("cookieUser", auth.getName());
        verify(filterChain, atLeastOnce()).doFilter(req, res);
    }

    @Test
    void doFilter_ignoresInvalidToken_exceptionsAreCaught() throws Exception {
        String token = "badtok";
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(jwtUtil.extraerUsername(token)).thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "expired"));

        filtro.doFilterInternal(req, res, filterChain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
        verify(filterChain).doFilter(req, res);
    }

    @Test
    void doFilter_usernameNull_doesNotAuthenticate() throws Exception {
        String token = "tok2";
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(jwtUtil.extraerUsername(token)).thenReturn(null);

        filtro.doFilterInternal(req, res, filterChain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
        verify(filterChain).doFilter(req, res);
    }

    @Test
    void doFilter_esValidoFalse_doesNotSetAuthentication() throws Exception {
        String token = "tok3";
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();

        when(jwtUtil.extraerUsername(token)).thenReturn("u2");
        when(jwtUtil.esValido(token, "u2")).thenReturn(false);
        UserDetails ud = User.withUsername("u2").password("p").authorities(Collections.emptyList()).build();
        when(usuarioDetallesService.loadUserByUsername("u2")).thenReturn(ud);

        filtro.doFilterInternal(req, res, filterChain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
        verify(filterChain).doFilter(req, res);
    }
}
