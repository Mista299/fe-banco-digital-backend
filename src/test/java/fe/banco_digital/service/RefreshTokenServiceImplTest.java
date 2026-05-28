package fe.banco_digital.service;

import fe.banco_digital.dto.LoginResponseDTO;
import fe.banco_digital.entity.RefreshToken;
import fe.banco_digital.entity.Usuario;
import fe.banco_digital.exception.TokenExpiradoException;
import fe.banco_digital.exception.TokenInvalidoException;
import fe.banco_digital.mapper.AutenticacionMapper;
import fe.banco_digital.repository.RefreshTokenRepository;
import fe.banco_digital.repository.UsuarioRepository;
import fe.banco_digital.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    JwtUtil jwtUtil;

    @Mock
    AutenticacionMapper autenticacionMapper;

    @InjectMocks
    RefreshTokenServiceImpl service;

    @BeforeEach
    void setUp() {
        // set expiracion a 7 dias para pruebas
        ReflectionTestUtils.setField(service, "expiracionRefreshDias", 7L);
    }

    @Test
    void crearRefreshToken_savesAndReturns() {
        Usuario u = new Usuario(); u.setIdUsuario(3L);
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(u));

        RefreshToken saved = new RefreshToken(); saved.setToken("abc");
        when(refreshTokenRepository.save(any())).thenReturn(saved);

        RefreshToken rt = service.crearRefreshToken(3L);
        assertNotNull(rt);
        assertEquals("abc", rt.getToken());
    }

    @Test
    void renovarToken_success_returnsLoginResponse() {
        Usuario u = new Usuario(); u.setIdUsuario(4L); u.setUsername("userx");
        RefreshToken existing = new RefreshToken(); existing.setToken("t1"); existing.setUsuario(u);
        existing.setFechaExpiracion(LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("t1")).thenReturn(Optional.of(existing));
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(u));

        when(jwtUtil.generarToken("userx")).thenReturn("acc");
        RefreshToken nuevo = new RefreshToken(); nuevo.setToken("t2");
        when(refreshTokenRepository.save(any())).thenReturn(nuevo);

        LoginResponseDTO out = new LoginResponseDTO(); out.setAccessToken("acc"); out.setRefreshToken("t2");
        when(autenticacionMapper.aLoginResponseDTO("acc", nuevo)).thenReturn(out);

        LoginResponseDTO res = service.renovarToken("t1");
        assertNotNull(res);
        assertEquals("acc", res.getAccessToken());
        verify(refreshTokenRepository).delete(existing);
    }

    @Test
    void renovarToken_expired_throws() {
        Usuario u = new Usuario(); u.setIdUsuario(4L); u.setUsername("userx");
        RefreshToken existing = new RefreshToken(); existing.setToken("t1"); existing.setUsuario(u);
        existing.setFechaExpiracion(LocalDateTime.now().minusDays(1));
        when(refreshTokenRepository.findByToken("t1")).thenReturn(Optional.of(existing));

        assertThrows(TokenExpiradoException.class, () -> service.renovarToken("t1"));
        verify(refreshTokenRepository).delete(existing);
    }

    @Test
    void revocarToken_deletesOrThrows() {
        RefreshToken existing = new RefreshToken(); existing.setToken("tx");
        when(refreshTokenRepository.findByToken("tx")).thenReturn(Optional.of(existing));

        service.revocarToken("tx");
        verify(refreshTokenRepository).delete(existing);

        when(refreshTokenRepository.findByToken("nope")).thenReturn(Optional.empty());
        assertThrows(TokenInvalidoException.class, () -> service.revocarToken("nope"));
    }
}
