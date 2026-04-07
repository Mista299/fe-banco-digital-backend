package fe.banco_digital.mapper;

import fe.banco_digital.dto.LoginResponseDTO;
import fe.banco_digital.entity.RefreshToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AutenticacionMapper {

    @Value("${jwt.expiracion-access-ms}")
    private long expiracionAccessMs;

    public LoginResponseDTO aLoginResponseDTO(String accessToken, RefreshToken refreshToken) {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setAccessToken(accessToken);
        dto.setRefreshToken(refreshToken.getToken());
        dto.setExpiraEn(expiracionAccessMs / 1000);
        return dto;
    }
}
