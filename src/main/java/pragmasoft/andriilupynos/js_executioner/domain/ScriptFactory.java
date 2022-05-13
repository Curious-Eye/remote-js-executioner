package pragmasoft.andriilupynos.js_executioner.domain;

import java.io.OutputStream;

/**
 * Anti-corruption layer around GraalJS
 */
public interface ScriptFactory {
    Script createScript(String sourceCode, OutputStream stdOut, OutputStream stdErr) throws ScriptException;

    @SuppressWarnings({"unused", "java:S106"})
    // to simplify testing
    default Script createScript(String sourceCode) throws ScriptException {
        return createScript(sourceCode, System.out, System.err);
    }

}
