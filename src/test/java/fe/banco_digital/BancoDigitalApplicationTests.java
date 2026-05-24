package fe.banco_digital;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"jwt.secreto=clave-secreta-para-tests-unitarios-minimo-32-caracteres",
		"GATEWAY_SECRET=test-gateway-secret"
})
class BancoDigitalApplicationTests {

	@Test
	void contextLoads() {
	}

}
