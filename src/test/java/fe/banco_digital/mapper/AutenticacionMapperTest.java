package fe.banco_digital.mapper;

import fe.banco_digital.dto.LoginResponseDTO;
import fe.banco_digital.entity.RefreshToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class AutenticacionMapperTest {

    @Test
    void aLoginResponseDTO_setsFields_correctly() throws Exception {
        AutenticacionMapper mapper = new AutenticacionMapper();

        // set private field expiracionAccessMs via reflection
        Field f = AutenticacionMapper.class.getDeclaredField("expiracionAccessMs");
        f.setAccessible(true);
        f.setLong(mapper, 60000L);

        RefreshToken rt = new RefreshToken();
        rt.setToken("refresh-123");

        LoginResponseDTO dto = mapper.aLoginResponseDTO("access-xyz", rt);

        assertEquals("access-xyz", dto.getAccessToken());
        assertEquals("refresh-123", dto.getRefreshToken());
        assertEquals(60L, dto.getExpiraEn());
    }
}
