package pragmasoft.andriilupynos.js_executioner.domain.exception;

import pragmasoft.andriilupynos.js_executioner.domain.ScriptException;

public class ScriptNotFoundException extends ScriptException {

    public ScriptNotFoundException(String name) {
        super("Script with name \"" + name + "\" does not exist");
    }

}
