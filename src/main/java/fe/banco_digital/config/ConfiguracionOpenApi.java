package fe.banco_digital.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracionOpenApi {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NexusBank API")
                        .version("v1")
                        .description("API REST del banco digital NexusBank — Sprint 3")
                        .contact(new Contact()
                                .name("Equipo Fábrica de Software")
                                .email("michael.tabares@udea.edu.co")));
    }
}
