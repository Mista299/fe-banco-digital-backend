package fe.banco_digital.service;

import fe.banco_digital.dto.LoginResponseDTO;
import fe.banco_digital.entity.RefreshToken;

public interface RefreshTokenService {

    RefreshToken crearRefreshToken(Long idUsuario);

    LoginResponseDTO renovarToken(String token);

    void revocarToken(String token);
}
