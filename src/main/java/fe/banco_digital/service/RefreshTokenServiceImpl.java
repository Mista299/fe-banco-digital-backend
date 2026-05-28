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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final AutenticacionMapper autenticacionMapper;

    @Value("${jwt.expiracion-refresh-dias}")
    private long expiracionRefreshDias;

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            UsuarioRepository usuarioRepository,
            JwtUtil jwtUtil,
            AutenticacionMapper autenticacionMapper) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.jwtUtil = jwtUtil;
        this.autenticacionMapper = autenticacionMapper;
    }

    @Override
    @Transactional
    public RefreshToken crearRefreshToken(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUsuario(usuario);
        refreshToken.setFechaExpiracion(LocalDateTime.now().plusDays(expiracionRefreshDias));

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public LoginResponseDTO renovarToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(TokenInvalidoException::new);

        if (refreshToken.isRevocado()) {
            throw new TokenInvalidoException();
        }

        if (refreshToken.getFechaExpiracion().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenExpiradoException();
        }

        String username = refreshToken.getUsuario().getUsername();

        // Token rotation: eliminar el viejo y crear uno nuevo
        refreshTokenRepository.delete(refreshToken);

        String nuevoAccessToken = jwtUtil.generarToken(username);
        RefreshToken nuevoRefreshToken = crearRefreshToken(refreshToken.getUsuario().getIdUsuario());

        return autenticacionMapper.aLoginResponseDTO(nuevoAccessToken, nuevoRefreshToken);
    }

    @Override
    @Transactional
    public void revocarToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(TokenInvalidoException::new);
        refreshTokenRepository.delete(refreshToken);
    }
}
