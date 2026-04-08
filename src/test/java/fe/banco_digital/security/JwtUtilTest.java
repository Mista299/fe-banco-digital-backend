package fe.banco_digital.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class JwtUtilTest {

    @Test
    void generarAndValidateToken() {
        JwtUtil util = new JwtUtil();
        // set a short secret and expiry
        ReflectionTestUtils.setField(util, "secreto", "01234567890123456789012345678901");
        ReflectionTestUtils.setField(util, "expiracionMs", 10000L);

        String token = util.generarToken("tester");
        assertNotNull(token);

        String username = util.extraerUsername(token);
        assertEquals("tester", username);

        assertTrue(util.esValido(token, "tester"));
    }
}
