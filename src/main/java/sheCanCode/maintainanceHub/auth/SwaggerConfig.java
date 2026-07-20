package sheCanCode.maintainanceHub.auth;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Maintenance Request System API")
                        .version("1.0.0")
                        .description("API Documentation for Maintenance Hub - A platform connecting customers with verified technicians for maintenance services")
                        .contact(new Contact()
                                .name("Maintenance Hub Team")
                                .email("support@maintenancehub.rw"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.maintenancerequest.rw")
                                .description("Production Server")
                ));
    }
}
