package pragmasoft.andriilupynos.js_executioner.exception;

import org.springframework.http.HttpStatus;

public class InvalidJSProvidedException extends BaseException {

    public InvalidJSProvidedException(String message) {
        super("invalid_js_provided_exception", HttpStatus.BAD_REQUEST, message);
    }

}
