package pragmasoft.andriilupynos.js_executioner.config;

import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.schema.WildcardType;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;
import java.util.Set;

@Configuration
class SwaggerConfig {

    @Autowired private TypeResolver typeResolver;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).select()
                .apis(RequestHandlerSelectors.basePackage("pragmasoft.andriilupynos.js_executioner"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(
                        new ApiInfoBuilder()
                                .title("JS executioner API")
                                .description("API of JS Executioner. By Andrii Lupynos")
                                .version("1.0")
                                .build()
                )
                .useDefaultResponseMessages(false)
                .genericModelSubstitutes(Mono.class)
                .produces(Set.of("application/json"))
                .alternateTypeRules(
                        AlternateTypeRules.newRule(
                                typeResolver.resolve(Flux.class, WildcardType.class),
                                typeResolver.resolve(List.class, WildcardType.class)
                        )
                );
    }

}
