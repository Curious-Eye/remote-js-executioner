package pragmasoft.andriilupynos.js_executioner.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pragmasoft.andriilupynos.js_executioner.domain.*;
import pragmasoft.andriilupynos.js_executioner.infrastructure.GraalJSScriptFactory;
import pragmasoft.andriilupynos.js_executioner.infrastructure.InMemoryScriptExecutionRepository;
import pragmasoft.andriilupynos.js_executioner.infrastructure.InMemoryScriptInfoRepository;

import java.util.concurrent.Executors;

@Configuration
public class BeanConfiguration {

    @Bean
    public ScriptService scriptService(
            ScriptFactory scriptFactory,
            ScriptInfoRepository scriptInfoRepository,
            ScriptExecutionRepository scriptExecutionRepository
    ) {
        return new DomainScriptService(
                scriptFactory,
                scriptInfoRepository,
                Executors.newFixedThreadPool(8),
                scriptExecutionRepository
        );
    }

    @Bean
    public ScriptFactory scriptFactory() {
        return new GraalJSScriptFactory();
    }

    @Bean
    public ScriptInfoRepository scriptInfoRepository() {
        return new InMemoryScriptInfoRepository();
    }

    @Bean
    public ScriptExecutionRepository scriptExecutionRepository() {
        return new InMemoryScriptExecutionRepository();
    }

}
