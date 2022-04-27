package pragmasoft.andriilupynos.js_executioner.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "JS executioner API",
                version = "0.2.0",
                description = "Documentation for APIs for JSExecutioner"
        )
)
class SwaggerConfig {

}
