package pragmasoft.andriilupynos.js_executioner.domain;

/**
 * Abstract base class for domain exceptions
 */
public abstract class ScriptException extends RuntimeException {

    protected ScriptException(String msg) {
        super(msg);
    }

}
