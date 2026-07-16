package sheCanCode.maintainanceHub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("MAINTENANCE HUB")
                .version("1.0")
                .description("Maintenance Hub API")
                .contact(new Contact()
                    .name("SheCanCode")
                    .email("noreply@hospital.com")));
    }
}
