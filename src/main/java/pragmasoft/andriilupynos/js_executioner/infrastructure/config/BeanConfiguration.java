package pragmasoft.andriilupynos.js_executioner.infrastructure.config;

import org.graalvm.polyglot.Engine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pragmasoft.andriilupynos.js_executioner.domain.service.DomainScriptService;
import pragmasoft.andriilupynos.js_executioner.domain.service.ScriptService;

import java.util.concurrent.Executors;

@Configuration
public class BeanConfiguration {

    @Bean
    public ScriptService scriptService(Engine engine) {
        return new DomainScriptService(
                engine,
                Executors.newScheduledThreadPool(8)
        );
    }

    @Bean(destroyMethod = "close")
    public Engine scriptEngine() {
        return Engine.newBuilder().build();
    }

}
