package pragmasoft.andriilupynos.js_executioner.exception;

import org.springframework.http.HttpStatus;

public class ScriptNotFoundException extends BaseException {

    public ScriptNotFoundException(String message) {
        super("script_not_found_exception", HttpStatus.NOT_FOUND, message);
    }

}
