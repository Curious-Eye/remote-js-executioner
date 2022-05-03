package pragmasoft.andriilupynos.js_executioner.service;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;
import org.graalvm.polyglot.Context;
import org.springframework.stereotype.Service;
import pragmasoft.andriilupynos.js_executioner.data.domain.Script;
import pragmasoft.andriilupynos.js_executioner.exception.InvalidJSProvidedException;
import reactor.core.publisher.Mono;

@Service
public class ScriptValidateService {

    private final GraalJSScriptEngine jsEngine =
            GraalJSScriptEngine.create(null, Context.newBuilder("js"));

    /**
     * Validates given script. If given script has invalid code (currently only accepts js code),
     * then an {@link InvalidJSProvidedException} exception will be thrown
     *
     * @param script script to validate
     * @see InvalidJSProvidedException
     */
    public Mono<Void> validate(Script script) {
        return Mono.fromCallable(() -> {
            try {
                jsEngine.compile(script.getCode());
            } catch(Exception e) {
                throw new InvalidJSProvidedException(e.getCause().getMessage());
            }
            return null;
        });
    }

}
