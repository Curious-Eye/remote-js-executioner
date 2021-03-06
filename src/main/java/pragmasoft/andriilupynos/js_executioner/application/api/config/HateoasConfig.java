package pragmasoft.andriilupynos.js_executioner.application.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.hateoas.server.core.DefaultLinkRelationProvider;
import pragmasoft.andriilupynos.js_executioner.application.api.dto.ScriptDto;
import pragmasoft.andriilupynos.js_executioner.application.api.dto.ScriptSimpleDto;

import javax.annotation.Nonnull;

@Configuration
public class HateoasConfig {

    @Bean
    public LinkRelationProvider relProvider() {
        return new CustomLinkRelationProvider();
    }

    public static class CustomLinkRelationProvider extends DefaultLinkRelationProvider {

        @Override
        @Nonnull
        public LinkRelation getCollectionResourceRelFor(@Nonnull Class<?> type) {
            if (ScriptSimpleDto.class.isAssignableFrom(type))
                return LinkRelation.of("scripts");

            return super.getCollectionResourceRelFor(type);
        }

        @Override
        @Nonnull
        public LinkRelation getItemResourceRelFor(@Nonnull Class<?> type) {
            if (ScriptDto.class.isAssignableFrom(type))
                return LinkRelation.of("script");

            return super.getItemResourceRelFor(type);
        }
    }

}
