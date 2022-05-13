package pragmasoft.andriilupynos.js_executioner.domain.exception;

import pragmasoft.andriilupynos.js_executioner.domain.ScriptException;

public class InvalidScriptProvidedException extends ScriptException {

    public InvalidScriptProvidedException(String parseErrMessage) {
        super("Invalid script provided. Details:\n" + parseErrMessage);
    }

}
