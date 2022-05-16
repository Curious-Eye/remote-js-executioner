package pragmasoft.andriilupynos.js_executioner.application.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "JS executioner API",
                version = "0.4.0",
                description = "Documentation for APIs for JSExecutioner"
        ),
        // Different servers to test CORS support via swagger-ui
        servers = {
                @Server(url = "http://localhost:8080"),
                @Server(url = "http://127.0.0.1:8080")
        }
)
class SwaggerConfig {

}
