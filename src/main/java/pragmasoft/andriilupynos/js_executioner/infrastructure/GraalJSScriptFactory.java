package pragmasoft.andriilupynos.js_executioner.infrastructure;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import pragmasoft.andriilupynos.js_executioner.domain.Script;
import pragmasoft.andriilupynos.js_executioner.domain.ScriptException;
import pragmasoft.andriilupynos.js_executioner.domain.ScriptFactory;
import pragmasoft.andriilupynos.js_executioner.domain.exception.InvalidScriptProvidedException;

import java.io.OutputStream;
import java.util.logging.ConsoleHandler;

public class GraalJSScriptFactory implements ScriptFactory {

    @Override
    public Script createScript(String sourceCode, OutputStream out, OutputStream err) throws ScriptException {
        return new GraalJSScript(sourceCode, out, err);
    }

    private static class GraalJSScript extends Script {

        private final Value value;

        protected GraalJSScript(String sourceCode, OutputStream out, OutputStream err) {
            super(sourceCode, out, err);
            try {
                // Could share one engine between Contexts
                var ctx =
                        Context.newBuilder("js")
                            .out(this.out)
                            .err(this.err)
                            .logHandler(new ConsoleHandler())
                            .build();
                this.value = ctx.parse("js", sourceCode);
            } catch(Exception e) {
                throw new InvalidScriptProvidedException(e.getMessage());
            }
        }

        /**
         * Closes execution context, stopping current execution if it is being performed
         */
        @Override
        public void close() {
            this.value.getContext().close(true);
        }

        @Override
        public void run() {
            this.value.executeVoid();
        }

    }

}
